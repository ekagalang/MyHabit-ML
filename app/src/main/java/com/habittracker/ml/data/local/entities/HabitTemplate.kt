package com.habittracker.ml.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_templates")
data class HabitTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val category: String,
    val icon: String,
    val defaultFrequency: String = "daily",
    val defaultReminderTime: String? = null,
    val tags: String = "" // CSV of tags
)