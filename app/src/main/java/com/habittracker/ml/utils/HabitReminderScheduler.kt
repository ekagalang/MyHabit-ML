package com.habittracker.ml.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.receivers.HabitReminderReceiver
import java.util.*

class HabitReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(habit: Habit) {
        if (!habit.reminderEnabled || habit.reminderTime == null) return

        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra("HABIT_ID", habit.id)
            putExtra("HABIT_NAME", habit.name)
            putExtra("HABIT_ICON", habit.icon)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateTriggerTime(habit.reminderTime)

        // Schedule repeating alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelReminder(habitId: Long) {
        val intent = Intent(context, HabitReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    private fun calculateTriggerTime(timeString: String): Long {
        val parts = timeString.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time has passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }
}