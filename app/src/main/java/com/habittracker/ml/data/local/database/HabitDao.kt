package com.habittracker.ml.data.local.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.data.local.entities.HabitWithCheckIns

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveHabits(): LiveData<List<Habit>>

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getAllActiveHabitsSync(): List<Habit>

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getActiveHabits(): List<Habit>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("UPDATE habits SET isActive = 0 WHERE id = :habitId")
    suspend fun softDeleteHabit(habitId: Long)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT COUNT(*) FROM habits WHERE isActive = 1")
    suspend fun getActiveHabitsCount(): Int

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitWithCheckIns(habitId: Long): HabitWithCheckIns?

    @Transaction
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getAllHabitsWithCheckIns(): List<HabitWithCheckIns>

    // Add these methods to HabitDao interface:

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllHabitsSync(): List<Habit>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitByIdSync(habitId: Long): Habit?

    // Sync-related queries
    @Query("SELECT * FROM habits WHERE isSynced = 0 AND isActive = 1")
    suspend fun getUnsyncedHabits(): List<Habit>

    @Query("UPDATE habits SET serverId = :serverId, isSynced = 1 WHERE id = :localId")
    suspend fun updateServerMapping(localId: Long, serverId: Long)

    @Query("SELECT * FROM habits WHERE serverId = :serverId LIMIT 1")
    suspend fun getHabitByServerId(serverId: Long): Habit?

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    suspend fun getAllHabitsIncludingInactive(): List<Habit>
}