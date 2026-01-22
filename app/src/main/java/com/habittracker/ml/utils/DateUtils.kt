package com.habittracker.ml.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }

    fun getCurrentTime(): String {
        return timeFormat.format(Date())
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
}