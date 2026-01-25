package com.habittracker.ml.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predictions")
data class Prediction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val generatedAt: Long = System.currentTimeMillis(),
    val predictionType: String, // "future_self", "best_time", "correlation"
    val overallScore: Float, // 0-100
    val description: String,
    val confidenceLevel: Float, // 0-1
    val validUntil: Long, // timestamp

    // JSON string untuk detail predictions per habit
    val habitPredictions: String? = null,
    val bestTimes: String? = null,
    val correlations: String? = null
)