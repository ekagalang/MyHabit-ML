package com.habittracker.ml.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.habittracker.ml.receivers.DailySummaryReceiver
import java.util.*

class DailySummaryScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleDailySummary(timeString: String) {
        Log.d("DailySummaryScheduler", "📅 Scheduling daily summary for: $timeString")

        val intent = Intent(context, DailySummaryReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_SUMMARY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateTriggerTime(timeString)
        val currentTime = System.currentTimeMillis()
        val delayMinutes = (triggerTime - currentTime) / 1000 / 60

        Log.d("DailySummaryScheduler", "⏳ Will trigger in: $delayMinutes minutes")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    // Use setRepeating for daily interval with exact timing
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                    Log.d("DailySummaryScheduler", "✅ Daily repeating alarm scheduled (API 31+)")
                } else {
                    Log.w("DailySummaryScheduler", "⚠️ Exact alarm permission missing")
                    // Fallback to inexact repeating alarm
                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                }
            } else {
                // For older Android versions, setRepeating works fine
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
                Log.d("DailySummaryScheduler", "✅ Daily repeating alarm scheduled")
            }
        } catch (e: Exception) {
            Log.e("DailySummaryScheduler", "❌ Error scheduling alarm", e)
        }
    }

    fun cancelDailySummary() {
        val intent = Intent(context, DailySummaryReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_SUMMARY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("DailySummaryScheduler", "❌ Daily summary cancelled")
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

    companion object {
        private const val DAILY_SUMMARY_REQUEST_CODE = 99999
    }
}
