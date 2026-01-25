package com.habittracker.ml.ui.insights

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.habittracker.ml.data.local.entities.Prediction
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.data.repository.PredictionRepository
import com.habittracker.ml.ml.HabitPredictor
import com.habittracker.ml.ml.models.BestTimeRecommendation
import com.habittracker.ml.ml.models.HabitPrediction
import com.habittracker.ml.utils.WorkManagerHelper
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class InsightsViewModel(
    private val habitRepository: HabitRepository,
    private val predictionRepository: PredictionRepository,
    private val context: Context
) : ViewModel() {

    private val gson = Gson()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _overallScore = MutableLiveData<Float>()
    val overallScore: LiveData<Float> = _overallScore

    private val _description = MutableLiveData<String>()
    val description: LiveData<String> = _description

    private val _confidence = MutableLiveData<Float>()
    val confidence: LiveData<Float> = _confidence

    private val _habitPredictions = MutableLiveData<List<HabitPrediction>>()
    val habitPredictions: LiveData<List<HabitPrediction>> = _habitPredictions

    private val _bestTimes = MutableLiveData<List<BestTimeRecommendation>>()
    val bestTimes: LiveData<List<BestTimeRecommendation>> = _bestTimes

    private val _lastUpdated = MutableLiveData<Long>()
    val lastUpdated: LiveData<Long> = _lastUpdated

    private val _hasData = MutableLiveData<Boolean>()
    val hasData: LiveData<Boolean> = _hasData

    init {
        loadPredictions()
    }

    fun loadPredictions() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Load latest future self prediction
                val futureSelfPrediction = predictionRepository.getLatestPredictionByType("future_self")

                if (futureSelfPrediction != null) {
                    // Parse and display prediction
                    _overallScore.value = futureSelfPrediction.overallScore
                    _description.value = futureSelfPrediction.description
                    _confidence.value = futureSelfPrediction.confidenceLevel
                    _lastUpdated.value = futureSelfPrediction.generatedAt

                    // Parse habit predictions
                    futureSelfPrediction.habitPredictions?.let { json ->
                        val type = object : TypeToken<List<HabitPrediction>>() {}.type
                        val predictions: List<HabitPrediction> = gson.fromJson(json, type)
                        _habitPredictions.value = predictions
                    }

                    _hasData.value = true
                } else {
                    _hasData.value = false
                }

                // Load best times
                val bestTimesPrediction = predictionRepository.getLatestPredictionByType("best_time")
                bestTimesPrediction?.bestTimes?.let { json ->
                    val type = object : TypeToken<List<BestTimeRecommendation>>() {}.type
                    val times: List<BestTimeRecommendation> = gson.fromJson(json, type)
                    _bestTimes.value = times
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _hasData.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateNow() {
        _isLoading.value = true
        WorkManagerHelper.scheduleImmediatePrediction(context)

        // Reload after 3 seconds (give worker time to complete)
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            loadPredictions()
        }
    }

    fun getFormattedLastUpdated(): String {
        val lastUpdatedTime = _lastUpdated.value ?: return "Never"
        val now = System.currentTimeMillis()
        val diff = now - lastUpdatedTime

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes minute${if (minutes > 1) "s" else ""} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours hour${if (hours > 1) "s" else ""} ago"
            }
            else -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days day${if (days > 1) "s" else ""} ago"
            }
        }
    }
}