package com.habittracker.ml.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.habittracker.ml.MainActivity
import com.habittracker.ml.R
import com.habittracker.ml.data.local.preferences.AppPreferences
import com.habittracker.ml.data.local.preferences.NotificationHistoryItem

object NotificationHelper {

    private const val CHANNEL_ID = "habit_reminders"
    private const val CHANNEL_NAME = "Habit Reminders"
    private const val CHANNEL_DESCRIPTION = "Daily reminders for your habits"
    private const val TYPE_HABIT = "habit_reminder"
    private const val TYPE_DAILY_SUMMARY = "daily_summary"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendHabitReminder(
        context: Context,
        habitId: Long,
        habitName: String,
        habitIcon: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "$habitIcon Time for $habitName!"
        val message = "Don't break your streak! Tap to check in."
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(habitId.toInt(), notification)
        logNotification(context, TYPE_HABIT, title, message)
    }

    fun sendDailySummary(context: Context, completedCount: Int, totalCount: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Daily Summary 📊"
        val message = "You've completed $completedCount of $totalCount habits today!"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(9999, notification)
        logNotification(context, TYPE_DAILY_SUMMARY, title, message)
    }

    private fun logNotification(
        context: Context,
        type: String,
        title: String,
        message: String
    ) {
        val prefs = AppPreferences(context)
        val item = NotificationHistoryItem(
            id = System.currentTimeMillis(),
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            type = type
        )
        prefs.addNotificationHistory(item)
    }
}
