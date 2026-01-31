package com.habittracker.ml.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "check_ins",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["habitId"])]
)
data class CheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val habitId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String, // format: "yyyy-MM-dd"
    val completedAt: String, // format: "HH:mm"

    val note: String? = null,
    val mood: String? = null, // "very_happy", "happy", "neutral", "sad", "very_sad"

    // Advanced tracking (NEW)
    val isLate: Boolean = false,
    val minutesLate: Int = 0,
    val energyLevel: Int? = null, // 1-5 scale
    val stressLevel: Int? = null, // 1-5 scale
    val location: String? = null, // "home", "work", "gym", "outdoor"
    val weather: String? = null // "sunny", "rainy", "cloudy", "cold"
)