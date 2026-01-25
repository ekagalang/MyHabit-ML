package com.habittracker.ml.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.preferences.AppPreferences
import com.habittracker.ml.utils.DateUtils
import com.habittracker.ml.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailySummaryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DailySummary", "🔔 Daily Summary Receiver triggered!")

        val appPreferences = AppPreferences(context)

        // Check if notifications and daily summary are enabled
        if (!appPreferences.notificationsEnabled || !appPreferences.dailySummaryEnabled) {
            Log.d("DailySummary", "❌ Notifications or daily summary disabled")
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = HabitDatabase.getDatabase(context)
                val today = DateUtils.getCurrentDate()

                // Get all active habits
                val habits = database.habitDao().getActiveHabits()
                val totalCount = habits.size

                // Count completed habits today
                var completedCount = 0
                for (habit in habits) {
                    val checkIn = database.checkInDao().getCheckInForDate(habit.id, today)
                    if (checkIn != null) {
                        completedCount++
                    }
                }

                Log.d("DailySummary", "📊 Completed: $completedCount / $totalCount")

                // Send notification
                NotificationHelper.sendDailySummary(context, completedCount, totalCount)

            } catch (e: Exception) {
                Log.e("DailySummary", "❌ Error generating summary", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
