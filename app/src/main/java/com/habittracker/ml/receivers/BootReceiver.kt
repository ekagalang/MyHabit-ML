package com.habittracker.ml.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.utils.HabitReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule all habit reminders after device reboot
            rescheduleAllReminders(context)
        }
    }

    private fun rescheduleAllReminders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = HabitDatabase.getDatabase(context)
            val habits = database.habitDao().getAllActiveHabits().value ?: emptyList()

            val scheduler = HabitReminderScheduler(context)
            habits.forEach { habit ->
                if (habit.reminderEnabled && habit.reminderTime != null) {
                    scheduler.scheduleReminder(habit)
                }
            }
        }
    }
}