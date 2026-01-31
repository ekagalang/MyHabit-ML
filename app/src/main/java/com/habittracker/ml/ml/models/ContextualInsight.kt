package com.habittracker.ml.ml.models

data class ContextualInsight(
    val type: String,
    val factor: String,
    val impact: String,
    val correlation: Float,
    val description: String
)