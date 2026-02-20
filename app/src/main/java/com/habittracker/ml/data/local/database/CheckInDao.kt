package com.habittracker.ml.data.local.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.habittracker.ml.data.local.entities.CheckIn

@Dao
interface CheckInDao {

    @Query("SELECT * FROM check_ins WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun getCheckInsForHabit(habitId: Long): LiveData<List<CheckIn>>

    @Query("SELECT * FROM check_ins WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCheckInForDate(habitId: Long, date: String): CheckIn?

    @Query("SELECT * FROM check_ins WHERE date = :date ORDER BY completedAt DESC")
    fun getCheckInsForDate(date: String): LiveData<List<CheckIn>>

    @Query("SELECT * FROM check_ins WHERE date = :date ORDER BY completedAt DESC")
    suspend fun getCheckInsForDateSync(date: String): List<CheckIn>

    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC")
    suspend fun getAllCheckIns(): List<CheckIn>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckIn): Long

    @Delete
    suspend fun deleteCheckIn(checkIn: CheckIn)

    @Query("SELECT COUNT(*) FROM check_ins WHERE habitId = :habitId")
    suspend fun getCheckInCountForHabit(habitId: Long): Int

    // Add these methods to CheckInDao interface:

    @Query("SELECT * FROM check_ins ORDER BY date DESC")
    fun getAllCheckInsSync(): List<CheckIn>

    @Query("SELECT * FROM check_ins WHERE habitId = :habitId AND date = :date LIMIT 1")
    fun getCheckInByDateSync(habitId: Long, date: String): CheckIn?

    @Query("""
        SELECT * FROM check_ins
        WHERE habitId = :habitId
        AND date >= :startDate
        AND date <= :endDate
        ORDER BY date ASC
    """)
    suspend fun getCheckInsBetweenDates(
        habitId: Long,
        startDate: String,
        endDate: String
    ): List<CheckIn>

    // Sync-related queries
    @Query("SELECT * FROM check_ins WHERE isSynced = 0")
    suspend fun getUnsyncedCheckIns(): List<CheckIn>

    @Query("UPDATE check_ins SET serverId = :serverId, isSynced = 1 WHERE id = :localId")
    suspend fun updateServerMapping(localId: Long, serverId: Long)

    @Query("SELECT * FROM check_ins WHERE serverId = :serverId LIMIT 1")
    suspend fun getCheckInByServerId(serverId: Long): CheckIn?
}
