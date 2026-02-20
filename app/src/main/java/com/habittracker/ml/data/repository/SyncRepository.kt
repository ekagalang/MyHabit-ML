package com.habittracker.ml.data.repository

import com.habittracker.ml.data.local.database.CheckInDao
import com.habittracker.ml.data.local.database.HabitDao
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.data.local.preferences.AuthPreferences
import com.habittracker.ml.data.remote.ApiService
import com.habittracker.ml.data.remote.dto.CheckInDto
import com.habittracker.ml.data.remote.dto.HabitDto
import com.habittracker.ml.data.remote.dto.SyncRequest

data class SyncResult(
    val success: Boolean,
    val habitsUploaded: Int = 0,
    val habitsDownloaded: Int = 0,
    val checkInsUploaded: Int = 0,
    val checkInsDownloaded: Int = 0,
    val error: String? = null
)

class SyncRepository(
    private val apiService: ApiService,
    private val habitDao: HabitDao,
    private val checkInDao: CheckInDao,
    private val authPreferences: AuthPreferences
) {

    suspend fun fullSync(): SyncResult {
        if (!authPreferences.isLoggedIn()) {
            return SyncResult(success = false, error = "Not logged in")
        }

        return try {
            // 1. Get unsynced local data
            val unsyncedHabits = habitDao.getUnsyncedHabits()
            val unsyncedCheckIns = checkInDao.getUnsyncedCheckIns()

            // 2. Convert to DTOs
            val habitDtos = unsyncedHabits.map { it.toDto() }
            val checkInDtos = unsyncedCheckIns.map { it.toDto() }

            // 3. Upload to server
            val syncRequest = SyncRequest(
                habits = habitDtos,
                checkIns = checkInDtos
            )

            val response = apiService.syncData(syncRequest)
            if (!response.isSuccessful || response.body()?.success != true) {
                return SyncResult(
                    success = false,
                    error = response.body()?.message ?: "Sync failed"
                )
            }

            val syncResponse = response.body()!!.data!!

            // 4. Update local records with server IDs from uploaded data
            syncResponse.habits.forEach { serverHabit ->
                // Find matching local habit by name (since local IDs differ from server)
                val localHabit = unsyncedHabits.find { it.name == serverHabit.name }
                if (localHabit != null) {
                    habitDao.updateServerMapping(localHabit.id, serverHabit.id)
                }
            }

            syncResponse.checkIns.forEach { serverCheckIn ->
                val localCheckIn = unsyncedCheckIns.find {
                    it.date == serverCheckIn.date && it.habitId == getLocalHabitIdForServer(serverCheckIn.habitId)
                }
                if (localCheckIn != null) {
                    checkInDao.updateServerMapping(localCheckIn.id, serverCheckIn.id)
                }
            }

            // 5. Download server data (full sync)
            val downloadResponse = apiService.getSyncData()
            var habitsDownloaded = 0
            var checkInsDownloaded = 0

            if (downloadResponse.isSuccessful && downloadResponse.body()?.success == true) {
                val exportData = downloadResponse.body()!!.data!!

                // Import habits from server that don't exist locally
                exportData.habits.forEach { serverHabit ->
                    val existingHabit = habitDao.getHabitByServerId(serverHabit.id)
                    if (existingHabit == null) {
                        val localId = habitDao.insertHabit(serverHabit.toLocalHabit())
                        habitDao.updateServerMapping(localId, serverHabit.id)
                        habitsDownloaded++
                    }
                }

                // Import check-ins from server
                exportData.checkIns.forEach { serverCheckIn ->
                    val existingCheckIn = checkInDao.getCheckInByServerId(serverCheckIn.id)
                    if (existingCheckIn == null) {
                        val localHabitId = getLocalHabitIdForServer(serverCheckIn.habitId)
                        if (localHabitId != null) {
                            val localId = checkInDao.insertCheckIn(
                                serverCheckIn.toLocalCheckIn(localHabitId)
                            )
                            checkInDao.updateServerMapping(localId, serverCheckIn.id)
                            checkInsDownloaded++
                        }
                    }
                }
            }

            // 6. Update last sync time
            authPreferences.setLastSyncTime(System.currentTimeMillis())

            SyncResult(
                success = true,
                habitsUploaded = syncResponse.habitsCreated + syncResponse.habitsUpdated,
                habitsDownloaded = habitsDownloaded,
                checkInsUploaded = syncResponse.checkInsCreated,
                checkInsDownloaded = checkInsDownloaded
            )
        } catch (e: Exception) {
            SyncResult(success = false, error = e.message ?: "Sync error")
        }
    }

    private suspend fun getLocalHabitIdForServer(serverHabitId: Long): Long? {
        return habitDao.getHabitByServerId(serverHabitId)?.id
    }

    fun getLastSyncTime(): Long = authPreferences.getLastSyncTime()
}

// Extension functions for mapping between local entities and DTOs

fun Habit.toDto(): HabitDto = HabitDto(
    id = serverId ?: 0,
    name = name,
    description = description,
    category = category,
    targetFrequency = targetFrequency,
    reminderTime = reminderTime,
    reminderEnabled = reminderEnabled,
    repeatDays = repeatDays,
    color = color,
    icon = icon,
    isActive = isActive
)

fun CheckIn.toDto(): CheckInDto = CheckInDto(
    id = serverId ?: 0,
    habitId = habitId,
    date = date,
    completedAt = completedAt,
    note = note,
    mood = mood,
    isLate = isLate,
    minutesLate = minutesLate,
    energyLevel = energyLevel,
    stressLevel = stressLevel,
    location = location,
    weather = weather
)

fun HabitDto.toLocalHabit(): Habit = Habit(
    name = name,
    description = description,
    category = category,
    targetFrequency = targetFrequency,
    reminderTime = reminderTime,
    reminderEnabled = reminderEnabled,
    repeatDays = repeatDays,
    color = color,
    icon = icon,
    isActive = isActive,
    serverId = id,
    isSynced = true
)

fun CheckInDto.toLocalCheckIn(localHabitId: Long): CheckIn = CheckIn(
    habitId = localHabitId,
    timestamp = System.currentTimeMillis(),
    date = date,
    completedAt = completedAt,
    note = note,
    mood = mood,
    isLate = isLate,
    minutesLate = minutesLate,
    energyLevel = energyLevel,
    stressLevel = stressLevel,
    location = location,
    weather = weather,
    serverId = id,
    isSynced = true
)
