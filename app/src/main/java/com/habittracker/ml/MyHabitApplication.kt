package com.habittracker.ml

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.habittracker.ml.data.local.preferences.AppPreferences
import com.habittracker.ml.utils.DailySummaryScheduler
import com.habittracker.ml.utils.NotificationHelper

class MyHabitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Schedule daily summary if enabled
        val appPreferences = AppPreferences(this)
        if (appPreferences.notificationsEnabled && appPreferences.dailySummaryEnabled) {
            DailySummaryScheduler(this).scheduleDailySummary(appPreferences.dailySummaryTime)
        }
    }
}