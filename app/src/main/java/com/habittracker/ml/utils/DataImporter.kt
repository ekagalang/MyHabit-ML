package com.habittracker.ml.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.data.repository.HabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataImporter(private val repository: HabitRepository) {

    private val gson = Gson()

    enum class ImportStrategy {
        SKIP,           // Skip if habit exists
        REPLACE,        // Replace existing habit
        MERGE           // Merge check-ins
    }

    data class ImportResult(
        val success: Boolean,
        val habitsImported: Int,
        val checkInsImported: Int,
        val errors: List<String> = emptyList()
    )

    suspend fun importFromJson(
        jsonString: String,
        strategy: ImportStrategy = ImportStrategy.SKIP
    ): ImportResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting JSON import with strategy: $strategy")

        try {
            val exportData = gson.fromJson(jsonString, DataExporter.ExportData::class.java)

            if (exportData == null) {
                return@withContext ImportResult(
                    success = false,
                    habitsImported = 0,
                    checkInsImported = 0,
                    errors = listOf("Invalid JSON format")
                )
            }

            val errors = mutableListOf<String>()
            var habitsImported = 0
            var checkInsImported = 0

            // Import habits
            exportData.habits.forEach { habit ->
                try {
                    val existing = repository.getHabitByIdSync(habit.id)

                    when {
                        existing == null -> {
                            repository.insertHabit(habit)
                            habitsImported++
                        }
                        strategy == ImportStrategy.REPLACE -> {
                            repository.updateHabit(habit)
                            habitsImported++
                        }
                        strategy == ImportStrategy.SKIP -> {
                            Log.d(TAG, "Skipping existing habit: ${habit.name}")
                        }
                    }
                } catch (e: Exception) {
                    errors.add("Failed to import habit '${habit.name}': ${e.message}")
                }
            }

            // Import check-ins
            exportData.checkIns.forEach { checkIn ->
                try {
                    val existing = repository.getCheckInByDateSync(checkIn.habitId, checkIn.date)

                    if (existing == null) {
                        repository.insertCheckIn(checkIn)
                        checkInsImported++
                    } else {
                        Log.d(TAG, "Check-in already exists for habit ${checkIn.habitId} on ${checkIn.date}")
                    }
                } catch (e: Exception) {
                    errors.add("Failed to import check-in: ${e.message}")
                }
            }

            Log.d(TAG, "Import completed: $habitsImported habits, $checkInsImported check-ins")

            return@withContext ImportResult(
                success = errors.isEmpty(),
                habitsImported = habitsImported,
                checkInsImported = checkInsImported,
                errors = errors
            )

        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON parse error", e)
            return@withContext ImportResult(
                success = false,
                habitsImported = 0,
                checkInsImported = 0,
                errors = listOf("Invalid JSON format: ${e.message}")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Import error", e)
            return@withContext ImportResult(
                success = false,
                habitsImported = 0,
                checkInsImported = 0,
                errors = listOf("Import failed: ${e.message}")
            )
        }
    }

    suspend fun validateJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val exportData = gson.fromJson(jsonString, DataExporter.ExportData::class.java)
            return@withContext exportData != null && exportData.habits.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Validation error", e)
            return@withContext false
        }
    }

    companion object {
        private const val TAG = "DataImporter"
    }
}