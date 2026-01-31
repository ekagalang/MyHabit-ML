package com.habittracker.ml.ml.models

data class TimeCorrelation(
    val hasData: Boolean,
    val optimalTimeWindow: String?,
    val lateCheckInRate: Float,
    val averageDelay: Int,
    val punctualityScore: Float,
    val insight: String
)