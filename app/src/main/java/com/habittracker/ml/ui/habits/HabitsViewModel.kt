package com.habittracker.ml.ui.habits

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.data.local.preferences.AppPreferences
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.utils.DateUtils
import com.habittracker.ml.utils.HabitReminderScheduler
import kotlinx.coroutines.launch

class HabitsViewModel(
    private val repository: HabitRepository,
    private val context: Context? = null
) : ViewModel() {

    val allHabits: LiveData<List<Habit>> = repository.getAllActiveHabits()

    fun insertHabit(habit: Habit, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val habitId = repository.insertHabit(habit)

            // Schedule reminder if enabled and notifications are on
            if (context != null) {
                val appPreferences = AppPreferences(context)
                if (appPreferences.notificationsEnabled &&
                    habit.reminderEnabled &&
                    habit.reminderTime != null) {
                    val habitWithId = habit.copy(id = habitId)
                    val scheduler = HabitReminderScheduler(context)
                    scheduler.scheduleReminder(habitWithId)
                }
            }

            onComplete?.invoke(habitId)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)

            // Reschedule reminder
            if (context != null) {
                val appPreferences = AppPreferences(context)
                val scheduler = HabitReminderScheduler(context)

                if (appPreferences.notificationsEnabled &&
                    habit.reminderEnabled &&
                    habit.reminderTime != null) {
                    scheduler.scheduleReminder(habit)
                } else {
                    scheduler.cancelReminder(habit.id)
                }
            }
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)

            // Cancel reminder
            if (context != null) {
                val scheduler = HabitReminderScheduler(context)
                scheduler.cancelReminder(habitId)
            }
        }
    }

    fun checkInHabit(habitId: Long, note: String? = null) {
        viewModelScope.launch {
            val checkIn = CheckIn(
                habitId = habitId,
                date = DateUtils.getCurrentDate(),
                completedAt = DateUtils.getCurrentTime(),
                note = note
            )
            repository.insertCheckIn(checkIn)
        }
    }

    suspend fun isHabitCheckedInToday(habitId: Long): Boolean {
        val today = DateUtils.getCurrentDate()
        return repository.getCheckInForDate(habitId, today) != null
    }
}