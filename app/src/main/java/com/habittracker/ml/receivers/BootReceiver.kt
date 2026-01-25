package com.habittracker.ml.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.preferences.AppPreferences
import com.habittracker.ml.utils.HabitReminderScheduler
import com.habittracker.ml.utils.DailySummaryScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule all habit reminders after device reboot
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    rescheduleAllReminders(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun rescheduleAllReminders(context: Context) {
        val database = HabitDatabase.getDatabase(context)
        val habits = database.habitDao().getActiveHabits()

        val scheduler = HabitReminderScheduler(context)
        habits.forEach { habit ->
            if (habit.reminderEnabled && habit.reminderTime != null) {
                scheduler.scheduleReminder(habit)
            }
        }

        // Reschedule daily summary
        val appPreferences = AppPreferences(context)
        if (appPreferences.notificationsEnabled && appPreferences.dailySummaryEnabled) {
            DailySummaryScheduler(context).scheduleDailySummary(appPreferences.dailySummaryTime)
        }
    }
}