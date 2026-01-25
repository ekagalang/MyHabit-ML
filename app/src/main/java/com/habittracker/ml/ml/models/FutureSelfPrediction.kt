package com.habittracker.ml.ml.models

data class FutureSelfPrediction(
    val overallScore: Float, // 0-100
    val description: String,
    val habitPredictions: List<HabitPrediction>,
    val confidence: Float, // 0-1
    val generatedAt: Long = System.currentTimeMillis()
)

data class HabitPrediction(
    val habitId: Long,
    val habitName: String,
    val predictionText: String,
    val completionRate: Float, // 0-1
    val trend: String, // "improving", "stable", "declining"
    val projectedStreak: Int
)

data class BestTimeRecommendation(
    val habitId: Long,
    val habitName: String,
    val recommendedTime: String, // "HH:mm"
    val successRate: Float, // 0-1
    val totalAttempts: Int
)

data class HabitCorrelation(
    val habit1Id: Long,
    val habit1Name: String,
    val habit2Id: Long,
    val habit2Name: String,
    val correlationStrength: Float, // -1 to 1
    val description: String
)