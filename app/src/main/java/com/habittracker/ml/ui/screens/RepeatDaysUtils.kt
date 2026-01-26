package com.habittracker.ml.ui.screens

private val defaultWeeklyOrder = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY
)

fun parseRepeatDays(repeatDays: String?): Set<DayOfWeek> {
    if (repeatDays.isNullOrBlank()) return emptySet()
    return repeatDays.split(",")
        .mapNotNull { value ->
            runCatching { DayOfWeek.valueOf(value.trim()) }.getOrNull()
        }
        .toSet()
}

fun serializeRepeatDays(days: Set<DayOfWeek>): String? {
    if (days.isEmpty()) return null
    return days.joinToString(",") { it.name }
}

fun defaultWeeklyDays(targetFrequency: Int): Set<DayOfWeek> {
    val clamped = targetFrequency.coerceIn(1, defaultWeeklyOrder.size)
    return defaultWeeklyOrder.take(clamped).toSet()
}
