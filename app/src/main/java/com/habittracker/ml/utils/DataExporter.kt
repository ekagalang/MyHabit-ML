package com.habittracker.ml.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.data.repository.HabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DataExporter(private val repository: HabitRepository) {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    data class ExportData(
        val exportDate: Long,
        val version: String = "1.0",
        val habits: List<Habit>,
        val checkIns: List<CheckIn>
    )

    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting JSON export...")

        val habits = repository.getAllHabitsSync()
        val checkIns = repository.getAllCheckInsSync()

        val exportData = ExportData(
            exportDate = System.currentTimeMillis(),
            habits = habits,
            checkIns = checkIns
        )

        val json = gson.toJson(exportData)
        Log.d(TAG, "JSON export completed: ${habits.size} habits, ${checkIns.size} check-ins")

        return@withContext json
    }

    suspend fun exportToCsv(): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting CSV export...")

        val habits = repository.getAllHabitsSync()
        val checkIns = repository.getAllCheckInsSync()

        val csv = StringBuilder()

        // Header
        csv.append("Type,HabitId,HabitName,Category,Description,Frequency,CreatedAt,CheckInDate,CheckInNote\n")

        // Habits
        habits.forEach { habit ->
            csv.append("HABIT,")
            csv.append("${habit.id},")
            csv.append("\"${habit.name.replace("\"", "\"\"")}\",")
            csv.append("\"${habit.category}\",")
            csv.append("\"${habit.description?.replace("\"", "\"\"") ?: ""}\",")
            csv.append("\"${habit.targetFrequency}\",")
            csv.append("${habit.createdAt},")
            csv.append(",,\n")
        }

        // Check-ins
        checkIns.forEach { checkIn ->
            csv.append("CHECKIN,")
            csv.append("${checkIn.habitId},")
            csv.append(",,,,")
            csv.append("${checkIn.timestamp},")
            csv.append("${checkIn.date},")
            csv.append("\"${checkIn.note?.replace("\"", "\"\"") ?: ""}\"\n")
        }

        Log.d(TAG, "CSV export completed")
        return@withContext csv.toString()
    }

    suspend fun saveToFile(context: Context, content: String, format: String): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "myhabit_backup_$timestamp.$format"

        val downloadsDir = context.getExternalFilesDir(null) ?: context.filesDir
        val file = File(downloadsDir, fileName)

        file.writeText(content)
        Log.d(TAG, "File saved: ${file.absolutePath}")

        return@withContext file
    }

    companion object {
        private const val TAG = "DataExporter"
    }
}