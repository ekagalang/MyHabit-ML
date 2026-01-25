package com.habittracker.ml.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import com.habittracker.ml.workers.PredictionWorker
import java.util.concurrent.TimeUnit

object WorkManagerHelper {

    private const val PREDICTION_WORK_NAME = "weekly_prediction_generation"

    fun scheduleWeeklyPredictions(context: Context) {
        Log.d("WorkManagerHelper", "📅 Scheduling weekly predictions...")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        val predictionRequest = PeriodicWorkRequestBuilder<PredictionWorker>(
            7, TimeUnit.DAYS, // Repeat every 7 days
            1, TimeUnit.HOURS  // Flex interval
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PREDICTION_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            predictionRequest
        )

        Log.d("WorkManagerHelper", "✅ Weekly predictions scheduled!")
    }

    fun scheduleImmediatePrediction(context: Context) {
        Log.d("WorkManagerHelper", "⚡ Scheduling immediate prediction...")

        val predictionRequest = OneTimeWorkRequestBuilder<PredictionWorker>()
            .build()

        WorkManager.getInstance(context).enqueue(predictionRequest)

        Log.d("WorkManagerHelper", "✅ Immediate prediction scheduled!")
    }

    fun cancelAllWork(context: Context) {
        Log.d("WorkManagerHelper", "🛑 Cancelling all work...")
        WorkManager.getInstance(context).cancelUniqueWork(PREDICTION_WORK_NAME)
        Log.d("WorkManagerHelper", "✅ All work cancelled!")
    }
}