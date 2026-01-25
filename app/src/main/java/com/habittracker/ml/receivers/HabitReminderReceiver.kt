package com.habittracker.ml.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.preferences.AppPreferences
import com.habittracker.ml.utils.HabitReminderScheduler
import com.habittracker.ml.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HabitTracker:ReminderWakeLock"
        )
        
        try {
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
            Log.d("HabitReminder", "🔔 Receiver triggered!")

            val habitId = intent.getLongExtra("HABIT_ID", -1L)
            val habitName = intent.getStringExtra("HABIT_NAME") ?: "Habit"
            val habitIcon = intent.getStringExtra("HABIT_ICON") ?: "🎯"

            Log.d("HabitReminder", "📝 Habit ID: $habitId")
            Log.d("HabitReminder", "📝 Habit Name: $habitName")

            if (habitId != -1L) {
                // Check if master notification switch is enabled
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val appPreferences = AppPreferences(context)

                        if (appPreferences.notificationsEnabled) {
                            Log.d("HabitReminder", "📤 Sending notification...")
                            NotificationHelper.sendHabitReminder(context, habitId, habitName, habitIcon)
                            Log.d("HabitReminder", "✅ Notification sent!")
                        } else {
                            Log.d("HabitReminder", "🔕 Master switch OFF, skipping notification")
                        }

                        // Always reschedule for the next day (even if master switch is off)
                        // This way, when user turns it back on, reminders will work
                        val database = HabitDatabase.getDatabase(context)
                        val habit = database.habitDao().getHabitById(habitId)
                        if (habit != null && habit.reminderEnabled && habit.reminderTime != null) {
                            Log.d("HabitReminder", "🔄 Rescheduling for next occurrence...")
                            HabitReminderScheduler(context).scheduleReminder(habit)
                        }
                    } catch (e: Exception) {
                        Log.e("HabitReminder", "❌ Failed to process reminder", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            } else {
                Log.e("HabitReminder", "❌ Invalid habit ID")
            }
        } catch (e: Exception) {
            Log.e("HabitReminder", "❌ Error in receiver", e)
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}