package com.habittracker.ml.data.repository

import androidx.lifecycle.LiveData
import com.habittracker.ml.data.local.database.CheckInDao
import com.habittracker.ml.data.local.database.HabitDao
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.data.local.entities.HabitWithCheckIns

class HabitRepository(
    private val habitDao: HabitDao,
    private val checkInDao: CheckInDao
) {

    // Habit operations
    fun getAllActiveHabits(): LiveData<List<Habit>> {
        return habitDao.getAllActiveHabits()
    }

    suspend fun getHabitById(habitId: Long): Habit? {
        return habitDao.getHabitById(habitId)
    }

    suspend fun insertHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habitId: Long) {
        habitDao.softDeleteHabit(habitId)
    }

    suspend fun getActiveHabitsCount(): Int {
        return habitDao.getActiveHabitsCount()
    }

    // CheckIn operations
    fun getCheckInsForHabit(habitId: Long): LiveData<List<CheckIn>> {
        return checkInDao.getCheckInsForHabit(habitId)
    }

    suspend fun getCheckInForDate(habitId: Long, date: String): CheckIn? {
        return checkInDao.getCheckInForDate(habitId, date)
    }

    fun getCheckInsForDate(date: String): LiveData<List<CheckIn>> {
        return checkInDao.getCheckInsForDate(date)
    }

    suspend fun insertCheckIn(checkIn: CheckIn): Long {
        return checkInDao.insertCheckIn(checkIn)
    }

    suspend fun deleteCheckIn(checkIn: CheckIn) {
        checkInDao.deleteCheckIn(checkIn)
    }

    suspend fun getCheckInCountForHabit(habitId: Long): Int {
        return checkInDao.getCheckInCountForHabit(habitId)
    }

    suspend fun getCheckInsBetweenDates(
        habitId: Long,
        startDate: String,
        endDate: String
    ): List<CheckIn> {
        return checkInDao.getCheckInsBetweenDates(habitId, startDate, endDate)
    }

    suspend fun getHabitWithCheckIns(habitId: Long): HabitWithCheckIns? {
        return habitDao.getHabitWithCheckIns(habitId)
    }

    suspend fun getAllHabitsWithCheckIns(): List<HabitWithCheckIns> {
        return habitDao.getAllHabitsWithCheckIns()
    }
}