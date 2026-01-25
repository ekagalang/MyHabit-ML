package com.habittracker.ml.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import com.habittracker.ml.utils.ThemeManager

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "habit_tracker_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_DEFAULT_REMINDER_TIME = "default_reminder_time"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DAILY_SUMMARY_ENABLED = "daily_summary_enabled"
        private const val KEY_DAILY_SUMMARY_TIME = "daily_summary_time"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }

    var defaultReminderTime: String
        get() = prefs.getString(KEY_DEFAULT_REMINDER_TIME, "08:00") ?: "08:00"
        set(value) = prefs.edit().putString(KEY_DEFAULT_REMINDER_TIME, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    var dailySummaryEnabled: Boolean
        get() = prefs.getBoolean(KEY_DAILY_SUMMARY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DAILY_SUMMARY_ENABLED, value).apply()

    var dailySummaryTime: String
        get() = prefs.getString(KEY_DAILY_SUMMARY_TIME, "20:00") ?: "20:00"
        set(value) = prefs.edit().putString(KEY_DAILY_SUMMARY_TIME, value).apply()

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    var themeMode: Int
        get() = prefs.getInt("theme_mode", ThemeManager.MODE_SYSTEM)
        set(value) = prefs.edit().putInt("theme_mode", value).apply()
}