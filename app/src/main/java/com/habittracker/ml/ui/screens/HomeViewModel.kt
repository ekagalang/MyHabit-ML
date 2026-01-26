package com.habittracker.ml.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.data.local.entities.HabitWithCheckIns
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.utils.DateUtils
import com.habittracker.ml.utils.HabitReminderScheduler
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
    private val context = application.applicationContext

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val database = HabitDatabase.getDatabase(application)
        repository = HabitRepository(database.habitDao(), database.checkInDao())

        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Get all active habits directly from database (suspend function)
            val database = HabitDatabase.getDatabase(getApplication())
            val habitDao = database.habitDao()

            // Use suspend function to get fresh data
            val habitsList = getAllActiveHabitsSync()

            val today = DateUtils.getCurrentDate()
            val todayCheckIns = getTodayCheckInsSync(today)

            // Calculate total streak
            var totalStreak = 0
            habitsList.forEach { habit ->
                val habitWithCheckIns = repository.getHabitWithCheckIns(habit.id)
                if (habitWithCheckIns != null) {
                    totalStreak += StreakCalculator.calculateCurrentStreak(habitWithCheckIns.checkIns)
                }
            }

            _uiState.value = HomeUiState(
                habits = habitsList,
                todayCheckIns = todayCheckIns,
                totalStreak = totalStreak,
                completedToday = todayCheckIns.size,
                totalHabits = habitsList.size,
                isLoading = false
            )

            println("📊 HomeViewModel: Loaded ${habitsList.size} habits")
        }
    }

    private suspend fun getAllActiveHabitsSync(): List<Habit> {
        return try {
            val database = HabitDatabase.getDatabase(getApplication())
            // Direct database query
            database.habitDao().getAllActiveHabitsSync()
        } catch (e: Exception) {
            println("❌ Error loading habits: ${e.message}")
            emptyList()
        }
    }

    private suspend fun getTodayCheckInsSync(date: String): List<CheckIn> {
        return try {
            val database = HabitDatabase.getDatabase(getApplication())
            database.checkInDao().getCheckInsForDateSync(date)
        } catch (e: Exception) {
            println("❌ Error loading check-ins: ${e.message}")
            emptyList()
        }
    }

    fun checkInHabit(habitId: Long) {
        viewModelScope.launch {
            val today = DateUtils.getCurrentDate()
            val existingCheckIn = repository.getCheckInForDate(habitId, today)

            if (existingCheckIn == null) {
                val checkIn = CheckIn(
                    habitId = habitId,
                    date = today,
                    completedAt = DateUtils.getCurrentTime()
                )
                repository.insertCheckIn(checkIn)
                val currentState = _uiState.value
                if (currentState.todayCheckIns.none { it.habitId == habitId }) {
                    val updatedToday = currentState.todayCheckIns + checkIn
                    _uiState.value = currentState.copy(
                        todayCheckIns = updatedToday,
                        completedToday = updatedToday.size
                    )
                }
                println("✅ Check-in created for habit $habitId")
                loadData() // Refresh
            } else {
                println("⚠️ Already checked in today")
            }
        }
    }

    fun isHabitCheckedInToday(habitId: Long): Boolean {
        return _uiState.value.todayCheckIns.any { it.habitId == habitId }
    }

    suspend fun getHabitWithCheckIns(habitId: Long): HabitWithCheckIns? {
        return repository.getHabitWithCheckIns(habitId)
    }

    suspend fun getAllHabitsWithCheckIns(): List<HabitWithCheckIns> {
        return repository.getAllHabitsWithCheckIns()
    }

    fun refreshData() {
        loadData()
    }

    fun insertHabit(habit: Habit, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val habitId = repository.insertHabit(habit)
            println("✅ Habit inserted with ID: $habitId")

            // Schedule reminder if enabled
            if (habit.reminderEnabled && habit.reminderTime != null) {
                val habitWithId = habit.copy(id = habitId)
                val scheduler = HabitReminderScheduler(context)
                scheduler.scheduleReminder(habitWithId)
            }

            // Refresh immediately
            loadData()

            onComplete(habitId)
        }
    }

    fun updateHabit(habit: Habit, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            println("✅ Habit updated: ${habit.name}")

            // Update reminder
            val scheduler = HabitReminderScheduler(context)
            if (habit.reminderEnabled && habit.reminderTime != null) {
                scheduler.scheduleReminder(habit)
            } else {
                scheduler.cancelReminder(habit.id)
            }

            // Refresh immediately
            loadData()

            onComplete()
        }
    }

    suspend fun getHabit(habitId: Long): Habit? {
        return try {
            val database = HabitDatabase.getDatabase(getApplication())
            database.habitDao().getHabitById(habitId)
        } catch (e: Exception) {
            println("❌ Error loading habit: ${e.message}")
            null
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                val database = HabitDatabase.getDatabase(getApplication())
                database.habitDao().softDeleteHabit(habitId)

                // Cancel reminder
                val scheduler = HabitReminderScheduler(context)
                scheduler.cancelReminder(habitId)

                println("✅ Habit deleted: $habitId")

                // Refresh immediately
                loadData()
            } catch (e: Exception) {
                println("❌ Error deleting habit: ${e.message}")
            }
        }
    }
}


