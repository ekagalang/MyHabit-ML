package com.habittracker.ml.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.receivers.HabitReminderReceiver
import java.util.*
import android.util.Log

class HabitReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(habit: Habit) {
        if (!habit.reminderEnabled || habit.reminderTime == null) {
            Log.d("ReminderScheduler", "❌ Reminder not enabled for: ${habit.name}")
            return
        }

        Log.d("ReminderScheduler", "📅 Scheduling reminder for: ${habit.name}")
        Log.d("ReminderScheduler", "⏰ Time: ${habit.reminderTime}")

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
        val currentTime = System.currentTimeMillis()
        val delayMinutes = (triggerTime - currentTime) / 1000 / 60

        Log.d("ReminderScheduler", "⏳ Will trigger in: $delayMinutes minutes")
        Log.d("ReminderScheduler", "🕐 Trigger at: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(triggerTime))}")

        // Schedule alarm - using setAlarmClock for highest priority and accuracy
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    // Use setAlarmClock for maximum accuracy and priority
                    // This shows in status bar and won't be delayed by battery optimization
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(
                        triggerTime,
                        pendingIntent
                    )
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                    Log.d("ReminderScheduler", "✅ AlarmClock scheduled (API 31+) - Highest priority")
                } else {
                    Log.w("ReminderScheduler", "⚠️ Exact alarm permission missing, falling back")
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Use setAlarmClock for API 21+ for best accuracy
                val alarmClockInfo = AlarmManager.AlarmClockInfo(
                    triggerTime,
                    pendingIntent
                )
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                Log.d("ReminderScheduler", "✅ AlarmClock scheduled (API 21+) - High priority")
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d("ReminderScheduler", "✅ Exact alarm scheduled (API 23+)")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d("ReminderScheduler", "✅ Exact alarm scheduled")
            }
        } catch (e: SecurityException) {
            Log.e("ReminderScheduler", "❌ SecurityException: Failed to schedule exact alarm", e)
            // Fallback attempt
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Log.d("ReminderScheduler", "✅ Fallback: Inexact alarm scheduled")
            } catch (e2: Exception) {
                Log.e("ReminderScheduler", "❌ Fatal: Could not schedule any alarm", e2)
            }
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "❌ Error scheduling alarm", e)
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