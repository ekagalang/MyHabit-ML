package com.habittracker.ml.ui.habits

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.habittracker.ml.data.repository.HabitRepository

class HabitsViewModelFactory(
    private val repository: HabitRepository,
    private val context: Context? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitsViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}