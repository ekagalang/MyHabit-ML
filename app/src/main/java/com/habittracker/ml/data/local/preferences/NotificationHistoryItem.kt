package com.habittracker.ml.data.local.preferences

data class NotificationHistoryItem(
    val id: Long,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: String
)
