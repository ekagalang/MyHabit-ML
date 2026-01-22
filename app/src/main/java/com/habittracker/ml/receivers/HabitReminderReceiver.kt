package com.habittracker.ml.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habittracker.ml.utils.NotificationHelper

class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra("HABIT_ID", -1L)
        val habitName = intent.getStringExtra("HABIT_NAME") ?: "Habit"
        val habitIcon = intent.getStringExtra("HABIT_ICON") ?: "🎯"

        if (habitId != -1L) {
            NotificationHelper.sendHabitReminder(context, habitId, habitName, habitIcon)
        }
    }
}