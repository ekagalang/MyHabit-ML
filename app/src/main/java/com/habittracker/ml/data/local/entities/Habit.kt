package com.habittracker.ml.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val description: String,
    val category: String, // health, productivity, learning, mindfulness, social
    val targetFrequency: Int, // target per week (e.g., 5 = 5x per week)

    val reminderTime: String? = null, // format: "HH:mm" (e.g., "08:00")
    val reminderEnabled: Boolean = false,

    val repeatDays: String? = null, // CSV of DayOfWeek names, e.g. "MONDAY,WEDNESDAY"

    val color: String = "#6366F1", // hex color
    val icon: String = "🎯", // emoji icon

    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,

    // Server sync fields
    val serverId: Long? = null,
    val isSynced: Boolean = false
)
