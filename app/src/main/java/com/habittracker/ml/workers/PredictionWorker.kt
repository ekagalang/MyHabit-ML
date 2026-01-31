package com.habittracker.ml.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.entities.Prediction
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.data.repository.PredictionRepository
import com.habittracker.ml.ml.HabitPredictor
import com.habittracker.ml.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PredictionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val gson = Gson()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("PredictionWorker", "🤖 Starting prediction generation...")

            // Get database instances
            val database = HabitDatabase.getDatabase(applicationContext)
            val habitRepository = HabitRepository(database.habitDao(), database.checkInDao(), database.habitTemplateDao())
            val predictionRepository = PredictionRepository(database.predictionDao())

            // Get all habits with check-ins
            val habitsWithCheckIns = habitRepository.getAllHabitsWithCheckIns()

            if (habitsWithCheckIns.isEmpty()) {
                Log.d("PredictionWorker", "⚠️ No habits found, skipping prediction")
                return@withContext Result.success()
            }

            Log.d("PredictionWorker", "📊 Found ${habitsWithCheckIns.size} habits")

            // Create predictor
            val predictor = HabitPredictor()

            // Generate Future Self prediction
            val futureSelfPrediction = predictor.predictFutureSelf(habitsWithCheckIns, days = 30)

            Log.d("PredictionWorker", "🎯 Overall Score: ${futureSelfPrediction.overallScore}")

            // Convert habit predictions to JSON
            val habitPredictionsJson = gson.toJson(futureSelfPrediction.habitPredictions)

            // Create prediction entity
            val validUntil = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)
            val prediction = Prediction(
                predictionType = "future_self",
                overallScore = futureSelfPrediction.overallScore,
                description = futureSelfPrediction.description,
                confidenceLevel = futureSelfPrediction.confidence,
                validUntil = validUntil,
                habitPredictions = habitPredictionsJson
            )

            // Save to database
            val predictionId = predictionRepository.insertPrediction(prediction)
            Log.d("PredictionWorker", "✅ Prediction saved with ID: $predictionId")

            // Generate best time recommendations
            val bestTimes = predictor.analyzeBestTimes(habitsWithCheckIns)
            if (bestTimes.isNotEmpty()) {
                val bestTimesJson = gson.toJson(bestTimes)
                val bestTimesPrediction = Prediction(
                    predictionType = "best_time",
                    overallScore = 0f,
                    description = "Best times for your habits",
                    confidenceLevel = 0.8f,
                    validUntil = validUntil,
                    bestTimes = bestTimesJson
                )
                predictionRepository.insertPrediction(bestTimesPrediction)
                Log.d("PredictionWorker", "⏰ Best times saved: ${bestTimes.size} recommendations")
            }

            // Generate habit correlations
            val correlations = predictor.findHabitCorrelations(habitsWithCheckIns)
            if (correlations.isNotEmpty()) {
                val correlationsJson = gson.toJson(correlations)
                val correlationsPrediction = Prediction(
                    predictionType = "correlation",
                    overallScore = 0f,
                    description = "Habit correlations discovered",
                    confidenceLevel = 0.7f,
                    validUntil = validUntil,
                    correlations = correlationsJson
                )
                predictionRepository.insertPrediction(correlationsPrediction)
                Log.d("PredictionWorker", "🔗 Correlations saved: ${correlations.size} connections")
            }

            // Send notification
            sendPredictionNotification(futureSelfPrediction.overallScore)

            // Clean up expired predictions
            predictionRepository.deleteExpiredPredictions()

            Log.d("PredictionWorker", "🎉 Prediction generation completed!")
            Result.success()

        } catch (e: Exception) {
            Log.e("PredictionWorker", "❌ Error generating predictions: ${e.message}", e)
            Result.retry()
        }
    }

    private fun sendPredictionNotification(score: Float) {
        val emoji = when {
            score > 80 -> "🌟"
            score > 60 -> "💪"
            score > 40 -> "🎯"
            else -> "🌱"
        }

        NotificationHelper.sendDailySummary(
            applicationContext,
            completedCount = score.toInt(),
            totalCount = 100
        )

        Log.d("PredictionWorker", "📬 Notification sent!")
    }
}