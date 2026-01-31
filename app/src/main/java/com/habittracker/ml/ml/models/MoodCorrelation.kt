package com.habittracker.ml.ml.models

data class MoodCorrelation(
    val hasData: Boolean,
    val bestMood: String?,
    val worstMood: String?,
    val moodSuccessRates: Map<String, Float>,
    val insight: String
)