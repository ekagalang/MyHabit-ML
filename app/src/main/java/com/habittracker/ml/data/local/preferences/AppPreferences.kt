package com.habittracker.ml.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
        private const val KEY_PROFILE_NAME = "profile_name"
        private const val KEY_PROFILE_EMAIL = "profile_email"
        private const val KEY_CUSTOM_CATEGORIES = "custom_categories"
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

    var profileName: String
        get() = prefs.getString(KEY_PROFILE_NAME, "Guest User") ?: "Guest User"
        set(value) = prefs.edit().putString(KEY_PROFILE_NAME, value).apply()

    var profileEmail: String
        get() = prefs.getString(KEY_PROFILE_EMAIL, "guest@myhabit.com") ?: "guest@myhabit.com"
        set(value) = prefs.edit().putString(KEY_PROFILE_EMAIL, value).apply()

    fun getCategories(): List<CustomCategory> {
        val json = prefs.getString(KEY_CUSTOM_CATEGORIES, null)
        val parsed = if (json.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<CustomCategory>>() {}.type
                Gson().fromJson<List<CustomCategory>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        val defaults = listOf(
            CustomCategory("health", "Health", "#10B981", isDefault = true, isEnabled = true),
            CustomCategory("mindfulness", "Mindfulness", "#8B5CF6", isDefault = true, isEnabled = true),
            CustomCategory("productivity", "Productivity", "#6366F1", isDefault = true, isEnabled = true),
            CustomCategory("fitness", "Fitness", "#F59E0B", isDefault = true, isEnabled = true)
        )

        var normalized = parsed
        if (normalized.isNotEmpty() && normalized.all { !it.isDefault && !it.isEnabled }) {
            normalized = normalized.map { it.copy(isEnabled = true) }
        }

        val defaultsById = defaults.associateBy { it.id }
        val mergedDefaults = defaults.map { def ->
            val stored = normalized.find { it.id == def.id }
            if (stored == null) {
                def
            } else {
                def.copy(
                    name = stored.name,
                    colorHex = stored.colorHex,
                    isEnabled = stored.isEnabled,
                    isDefault = true
                )
            }
        }

        val custom = normalized.filter { !defaultsById.containsKey(it.id) }
        return mergedDefaults + custom
    }

    fun setCategories(categories: List<CustomCategory>) {
        val json = Gson().toJson(categories)
        prefs.edit().putString(KEY_CUSTOM_CATEGORIES, json).apply()
    }
}
