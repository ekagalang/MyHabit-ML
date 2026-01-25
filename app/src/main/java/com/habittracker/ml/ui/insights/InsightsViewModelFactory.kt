package com.habittracker.ml.ui.insights

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.data.repository.PredictionRepository

class InsightsViewModelFactory(
    private val habitRepository: HabitRepository,
    private val predictionRepository: PredictionRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InsightsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InsightsViewModel(habitRepository, predictionRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}