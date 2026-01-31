package com.habittracker.ml.ml.models

data class AdvancedPrediction(
    val habitId: Long,
    val habitName: String,
    val moodCorrelation: MoodCorrelation,
    val timeCorrelation: TimeCorrelation,
    val contextualInsights: List<ContextualInsight>,
    val optimalConditions: Map<String, String>,
    val riskFactors: List<String>,
    val overallSuccessScore: Float,
    val recommendation: String
)