package com.habittracker.ml.utils

import com.habittracker.ml.data.local.entities.CheckIn
import java.text.SimpleDateFormat
import java.util.*

object StreakCalculator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Calculate current streak from check-ins
     * Returns number of consecutive days with check-ins (ending today)
     */
    fun calculateCurrentStreak(checkIns: List<CheckIn>): Int {
        if (checkIns.isEmpty()) return 0

        // Get unique dates and sort descending
        val uniqueDates = checkIns
            .map { it.date }
            .distinct()
            .sortedDescending()

        if (uniqueDates.isEmpty()) return 0

        val today = DateUtils.getCurrentDate()
        val yesterday = getYesterdayDate()

        // Check if there's a check-in today or yesterday
        val lastCheckIn = uniqueDates.first()
        if (lastCheckIn != today && lastCheckIn != yesterday) {
            return 0 // Streak broken
        }

        // Count consecutive days
        var streak = 0
        var currentDate = if (lastCheckIn == today) today else yesterday

        for (date in uniqueDates) {
            if (date == currentDate) {
                streak++
                currentDate = getPreviousDate(currentDate)
            } else if (date < currentDate) {
                // Found a gap
                break
            }
        }

        return streak
    }

    /**
     * Calculate longest streak ever
     */
    fun calculateLongestStreak(checkIns: List<CheckIn>): Int {
        if (checkIns.isEmpty()) return 0

        val uniqueDates = checkIns
            .map { it.date }
            .distinct()
            .sorted()

        if (uniqueDates.isEmpty()) return 0
        if (uniqueDates.size == 1) return 1

        var maxStreak = 1
        var currentStreak = 1

        for (i in 1 until uniqueDates.size) {
            val prevDate = uniqueDates[i - 1]
            val currDate = uniqueDates[i]

            if (isConsecutiveDates(prevDate, currDate)) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }

        return maxStreak
    }

    /**
     * Calculate completion rate for a time period
     */
    fun calculateCompletionRate(
        checkIns: List<CheckIn>,
        targetFrequency: Int,
        days: Int = 30
    ): Float {
        val startDate = getDateDaysAgo(days)
        val relevantCheckIns = checkIns.filter { it.date >= startDate }

        val uniqueDays = relevantCheckIns.map { it.date }.distinct().size
        val expectedDays = (targetFrequency * days) / 7f

        return if (expectedDays > 0) {
            (uniqueDays / expectedDays).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    private fun isConsecutiveDates(date1: String, date2: String): Boolean {
        val nextDay = getNextDate(date1)
        return nextDay == date2
    }

    private fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(calendar.time)
    }

    private fun getPreviousDate(date: String): String {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(date) ?: return date
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(calendar.time)
    }

    private fun getNextDate(date: String): String {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(date) ?: return date
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return dateFormat.format(calendar.time)
    }

    private fun getDateDaysAgo(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return dateFormat.format(calendar.time)
    }
}