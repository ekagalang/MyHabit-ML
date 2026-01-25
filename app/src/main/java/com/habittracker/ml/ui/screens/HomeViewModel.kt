package com.habittracker.ml.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.utils.DateUtils
import com.habittracker.ml.utils.StreakCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val habits: List<Habit> = emptyList(),
    val todayCheckIns: List<CheckIn> = emptyList(),
    val totalStreak: Int = 0,
    val completedToday: Int = 0,
    val totalHabits: Int = 0,
    val isLoading: Boolean = true
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val database = HabitDatabase.getDatabase(application)
        repository = HabitRepository(database.habitDao(), database.checkInDao())

        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Get all habits
            val habits = repository.getAllActiveHabits().value ?: emptyList()

            // Get today's check-ins
            val today = DateUtils.getCurrentDate()
            val todayCheckIns = repository.getCheckInsForDate(today).value ?: emptyList()

            // Calculate total streak (sum of all habit streaks)
            var totalStreak = 0
            habits.forEach { habit ->
                val habitWithCheckIns = repository.getHabitWithCheckIns(habit.id)
                if (habitWithCheckIns != null) {
                    totalStreak += StreakCalculator.calculateCurrentStreak(habitWithCheckIns.checkIns)
                }
            }

            _uiState.value = HomeUiState(
                habits = habits,
                todayCheckIns = todayCheckIns,
                totalStreak = totalStreak,
                completedToday = todayCheckIns.size,
                totalHabits = habits.size,
                isLoading = false
            )
        }
    }

    fun checkInHabit(habitId: Long) {
        viewModelScope.launch {
            // Check if already checked in today
            val today = DateUtils.getCurrentDate()
            val existingCheckIn = repository.getCheckInForDate(habitId, today)

            if (existingCheckIn == null) {
                val checkIn = CheckIn(
                    habitId = habitId,
                    date = today,
                    completedAt = DateUtils.getCurrentTime()
                )
                repository.insertCheckIn(checkIn)
                loadData() // Refresh data
            }
        }
    }

    fun isHabitCheckedInToday(habitId: Long): Boolean {
        return _uiState.value.todayCheckIns.any { it.habitId == habitId }
    }

    fun refreshData() {
        loadData()
    }
}