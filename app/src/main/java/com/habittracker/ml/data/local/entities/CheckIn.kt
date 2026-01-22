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
    val date: String, // format: "yyyy-MM-dd" (e.g., "2025-01-11")
    val completedAt: String, // format: "HH:mm" (e.g., "14:30")

    val note: String? = null,
    val mood: Int? = null // 1-5 scale (optional for future)
)