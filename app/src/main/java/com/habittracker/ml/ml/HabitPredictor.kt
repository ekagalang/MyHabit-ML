package com.habittracker.ml.ml

import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.HabitWithCheckIns
import com.habittracker.ml.ml.models.BestTimeRecommendation
import com.habittracker.ml.ml.models.FutureSelfPrediction
import com.habittracker.ml.ml.models.HabitCorrelation
import com.habittracker.ml.ml.models.HabitPrediction
import com.habittracker.ml.utils.StreakCalculator
import kotlin.math.roundToInt

class HabitPredictor {

    /**
     * Generate Future Self prediction based on current habits
     */
    fun predictFutureSelf(
        habitsWithCheckIns: List<HabitWithCheckIns>,
        days: Int = 30
    ): FutureSelfPrediction {

        if (habitsWithCheckIns.isEmpty()) {
            return FutureSelfPrediction(
                overallScore = 0f,
                description = "Start creating habits to see your future self prediction!",
                habitPredictions = emptyList(),
                confidence = 0f
            )
        }

        val habitPredictions = mutableListOf<HabitPrediction>()
        var totalScore = 0f

        habitsWithCheckIns.forEach { habitData ->
            val completionRate = calculateCompletionRate(habitData)
            val consistency = calculateConsistency(habitData)
            val trend = calculateTrend(habitData)
            val currentStreak = StreakCalculator.calculateCurrentStreak(habitData.checkIns)

            // Calculate habit score (0-10)
            val habitScore = (completionRate * 0.6f + consistency * 0.4f) * 10f
            totalScore += habitScore

            // Generate prediction text
            val predictionText = generateHabitPrediction(
                habitData.habit.name,
                completionRate,
                consistency,
                trend
            )

            // Project future streak
            val projectedStreak = if (completionRate > 0.7f) {
                currentStreak + (days * completionRate * 0.8f).roundToInt()
            } else {
                currentStreak
            }

            habitPredictions.add(
                HabitPrediction(
                    habitId = habitData.habit.id,
                    habitName = habitData.habit.name,
                    predictionText = predictionText,
                    completionRate = completionRate,
                    trend = trend,
                    projectedStreak = projectedStreak
                )
            )
        }

        // Calculate overall score (0-100)
        val overallScore = (totalScore / habitsWithCheckIns.size).coerceIn(0f, 100f)

        // Generate description
        val description = generateOverallDescription(overallScore, habitPredictions.size)

        // Calculate confidence (more data = higher confidence)
        val totalCheckIns = habitsWithCheckIns.sumOf { it.checkIns.size }
        val confidence = calculateConfidence(totalCheckIns, habitsWithCheckIns.size)

        return FutureSelfPrediction(
            overallScore = overallScore,
            description = description,
            habitPredictions = habitPredictions,
            confidence = confidence
        )
    }

    /**
     * Analyze best times for habits based on check-in history
     */
    fun analyzeBestTimes(habitsWithCheckIns: List<HabitWithCheckIns>): List<BestTimeRecommendation> {
        val recommendations = mutableListOf<BestTimeRecommendation>()

        habitsWithCheckIns.forEach { habitData ->
            if (habitData.checkIns.size < 5) return@forEach // Need at least 5 check-ins

            // Group check-ins by hour
            val checkInsByHour = habitData.checkIns.groupBy { checkIn ->
                checkIn.completedAt.split(":")[0].toIntOrNull() ?: 0
            }

            // Find hour with most check-ins
            val bestHour = checkInsByHour.maxByOrNull { it.value.size }

            bestHour?.let { (hour, checkIns) ->
                val successRate = checkIns.size.toFloat() / habitData.checkIns.size

                if (successRate > 0.3f) { // At least 30% of check-ins at this hour
                    recommendations.add(
                        BestTimeRecommendation(
                            habitId = habitData.habit.id,
                            habitName = habitData.habit.name,
                            recommendedTime = String.format("%02d:00", hour),
                            successRate = successRate,
                            totalAttempts = checkIns.size
                        )
                    )
                }
            }
        }

        return recommendations.sortedByDescending { it.successRate }
    }

    /**
     * Find correlations between habits
     */
    fun findHabitCorrelations(habitsWithCheckIns: List<HabitWithCheckIns>): List<HabitCorrelation> {
        val correlations = mutableListOf<HabitCorrelation>()

        if (habitsWithCheckIns.size < 2) return correlations

        // Compare each pair of habits
        for (i in habitsWithCheckIns.indices) {
            for (j in i + 1 until habitsWithCheckIns.size) {
                val habit1 = habitsWithCheckIns[i]
                val habit2 = habitsWithCheckIns[j]

                val correlation = calculateCorrelation(habit1.checkIns, habit2.checkIns)

                if (kotlin.math.abs(correlation) > 0.5f) { // Significant correlation
                    val description = if (correlation > 0) {
                        "When you do ${habit1.habit.name}, you're ${(correlation * 100).roundToInt()}% more likely to do ${habit2.habit.name}"
                    } else {
                        "These habits rarely happen on the same day"
                    }

                    correlations.add(
                        HabitCorrelation(
                            habit1Id = habit1.habit.id,
                            habit1Name = habit1.habit.name,
                            habit2Id = habit2.habit.id,
                            habit2Name = habit2.habit.name,
                            correlationStrength = correlation,
                            description = description
                        )
                    )
                }
            }
        }

        return correlations.sortedByDescending { kotlin.math.abs(it.correlationStrength) }
    }

    // ========== PRIVATE HELPER FUNCTIONS ==========

    private fun calculateCompletionRate(habitData: HabitWithCheckIns): Float {
        return StreakCalculator.calculateCompletionRate(
            habitData.checkIns,
            habitData.habit.targetFrequency,
            days = 30
        )
    }

    private fun calculateConsistency(habitData: HabitWithCheckIns): Float {
        if (habitData.checkIns.size < 2) return 0f

        val uniqueDates = habitData.checkIns.map { it.date }.distinct().sorted()
        if (uniqueDates.size < 2) return 0f

        // Calculate variance in gaps between check-ins
        val gaps = mutableListOf<Int>()
        for (i in 1 until uniqueDates.size) {
            val gap = daysBetween(uniqueDates[i - 1], uniqueDates[i])
            gaps.add(gap)
        }

        val avgGap = gaps.average()
        val variance = gaps.map { (it - avgGap) * (it - avgGap) }.average()

        // Lower variance = higher consistency
        return (1f / (1f + variance.toFloat() / 10f)).coerceIn(0f, 1f)
    }

    private fun calculateTrend(habitData: HabitWithCheckIns): String {
        if (habitData.checkIns.size < 4) return "stable"

        val sortedCheckIns = habitData.checkIns.sortedBy { it.timestamp }
        val midPoint = sortedCheckIns.size / 2

        val firstHalf = sortedCheckIns.take(midPoint)
        val secondHalf = sortedCheckIns.takeLast(midPoint)

        val firstHalfUniqueDays = firstHalf.map { it.date }.distinct().size
        val secondHalfUniqueDays = secondHalf.map { it.date }.distinct().size

        return when {
            secondHalfUniqueDays > firstHalfUniqueDays * 1.2 -> "improving"
            secondHalfUniqueDays < firstHalfUniqueDays * 0.8 -> "declining"
            else -> "stable"
        }
    }

    private fun generateHabitPrediction(
        name: String,
        completionRate: Float,
        consistency: Float,
        trend: String
    ): String {
        return when {
            completionRate > 0.8f && consistency > 0.7f ->
                "$name: You'll master this habit! 🔥"
            completionRate > 0.6f && trend == "improving" ->
                "$name: Great progress, keep it up! 📈"
            completionRate > 0.4f ->
                "$name: You're on track! 💪"
            trend == "improving" ->
                "$name: Momentum is building! 🚀"
            else ->
                "$name: Needs extra effort 🎯"
        }
    }

    private fun generateOverallDescription(score: Float, habitCount: Int): String {
        return when {
            score > 80 -> """
                🌟 AMAZING! In 30 days, you will:
                • Have $habitCount solid habits
                • Consistency becomes lifestyle
                • Feel more productive & happy
                • Be an inspiration to others
            """.trimIndent()

            score > 60 -> """
                💪 GREAT PROGRESS! In 30 days:
                • ${(habitCount * 0.7).roundToInt()}-$habitCount habits stick
                • Starting to feel the benefits
                • Overall trending positive
                • Building momentum
            """.trimIndent()

            score > 40 -> """
                🎯 KEEP GOING! In 30 days:
                • ${(habitCount * 0.5).roundToInt()}-${(habitCount * 0.7).roundToInt()} habits forming
                • Progress is happening
                • Don't give up!
                • Consistency > Perfection
            """.trimIndent()

            else -> """
                🌱 JUST STARTED! In 30 days:
                • Focus on 1-2 habits first
                • Build momentum slowly
                • Celebrate small wins
                • You're building something great
            """.trimIndent()
        }
    }

    private fun calculateConfidence(totalCheckIns: Int, habitCount: Int): Float {
        val dataScore = (totalCheckIns.toFloat() / (habitCount * 30f)).coerceIn(0f, 1f)
        val habitScore = (habitCount.toFloat() / 5f).coerceIn(0f, 1f)
        return (dataScore * 0.7f + habitScore * 0.3f).coerceIn(0.1f, 0.95f)
    }

    private fun calculateCorrelation(checkIns1: List<CheckIn>, checkIns2: List<CheckIn>): Float {
        val dates1 = checkIns1.map { it.date }.toSet()
        val dates2 = checkIns2.map { it.date }.toSet()

        val commonDates = dates1.intersect(dates2).size
        val totalDates = dates1.union(dates2).size

        if (totalDates == 0) return 0f

        // Jaccard similarity
        return commonDates.toFloat() / totalDates.toFloat()
    }

    private fun daysBetween(date1: String, date2: String): Int {
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val d1 = format.parse(date1)?.time ?: 0
            val d2 = format.parse(date2)?.time ?: 0
            ((d2 - d1) / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            1
        }
    }
}