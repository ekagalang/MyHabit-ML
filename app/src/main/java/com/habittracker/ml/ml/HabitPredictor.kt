package com.habittracker.ml.ml

import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.HabitWithCheckIns
import com.habittracker.ml.ml.models.AdvancedPrediction
import com.habittracker.ml.ml.models.BestTimeRecommendation
import com.habittracker.ml.ml.models.ContextualInsight
import com.habittracker.ml.ml.models.FutureSelfPrediction
import com.habittracker.ml.ml.models.HabitCorrelation
import com.habittracker.ml.ml.models.HabitPrediction
import com.habittracker.ml.ml.models.MoodCorrelation
import com.habittracker.ml.ml.models.TimeCorrelation
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

    /**
     * Advanced analysis with mood, timing, and context
     */
    fun analyzeHabitAdvanced(habitData: HabitWithCheckIns): AdvancedPrediction {
        return AdvancedPrediction(
            habitId = habitData.habit.id,
            habitName = habitData.habit.name,
            moodCorrelation = analyzeMoodImpact(habitData.checkIns),
            timeCorrelation = analyzeTimeImpact(habitData),
            contextualInsights = analyzeContextualFactors(habitData.checkIns),
            optimalConditions = findOptimalConditions(habitData.checkIns),
            riskFactors = identifyRiskFactors(habitData),
            overallSuccessScore = calculateAdvancedScore(habitData),
            recommendation = generateAdvancedRecommendation(habitData)
        )
    }

    private fun analyzeMoodImpact(checkIns: List<CheckIn>): MoodCorrelation {
        val withMood = checkIns.filter { !it.mood.isNullOrBlank() }

        if (withMood.isEmpty()) {
            return MoodCorrelation(false, null, null, emptyMap(), "Track mood to discover patterns! 😊")
        }

        val rates = withMood.mapNotNull { it.mood }
            .groupBy { it }
            .mapValues { (_, list) -> list.size.toFloat() / withMood.size * 100f }

        val best = rates.maxByOrNull { it.value }
        val worst = rates.minByOrNull { it.value }

        val insight = when {
            best != null && best.value > 30f ->
                "${String.format("%.0f", best.value)}% success when ${getMoodEmoji(best.key)}. Schedule during happy times!"
            worst != null && worst.value < 10f ->
                "Struggles when ${getMoodEmoji(worst.key)}. Be gentle on those days."
            else -> "Mood affects this habit. Keep tracking!"
        }

        return MoodCorrelation(true, best?.key, worst?.key, rates, insight)
    }

    private fun analyzeTimeImpact(habitData: HabitWithCheckIns): TimeCorrelation {
        val checkIns = habitData.checkIns
        if (checkIns.isEmpty()) {
            return TimeCorrelation(false, null, 0f, 0, 100f, "Track times to optimize schedule! ⏰")
        }

        val lateCount = checkIns.count { it.isLate }
        val lateRate = lateCount.toFloat() / checkIns.size * 100f
        val avgDelay = checkIns.sumOf { it.minutesLate } / checkIns.size
        val punctuality = ((checkIns.size - lateCount).toFloat() / checkIns.size) * 100f

        val timeGroups = checkIns.groupBy { getTimeCategory(it.completedAt) }
            .mapValues { it.value.size }
        val optimal = timeGroups.maxByOrNull { it.value }?.key

        val insight = when {
            punctuality > 80f -> "Excellent! ${String.format("%.0f", punctuality)}% punctual 🎯"
            lateRate > 50f -> "Late ${String.format("%.0f", lateRate)}% - try earlier reminders"
            avgDelay > 60 -> "Avg ${avgDelay}min late. Try $optimal instead."
            else -> "Try $optimal for best consistency"
        }

        return TimeCorrelation(true, optimal, lateRate, avgDelay, punctuality, insight)
    }

    private fun analyzeContextualFactors(checkIns: List<CheckIn>): List<ContextualInsight> {
        val insights = mutableListOf<ContextualInsight>()

        // Energy
        val energy = checkIns.filter { it.energyLevel != null }
        if (energy.isNotEmpty()) {
            val avg = energy.mapNotNull { it.energyLevel }.average()
            val high = energy.count { (it.energyLevel ?: 0) >= 4 }
            val rate = high.toFloat() / energy.size * 100f

            insights.add(ContextualInsight(
                "energy", "Energy Level",
                if (avg >= 3.5) "positive" else "negative",
                rate,
                when {
                    avg >= 4.0 -> "High energy = success! ⚡"
                    avg >= 3.0 -> "Moderate energy works well"
                    else -> "Low energy hurts consistency"
                }
            ))
        }

        // Stress
        val stress = checkIns.filter { it.stressLevel != null }
        if (stress.isNotEmpty()) {
            val avg = stress.mapNotNull { it.stressLevel }.average()
            val low = stress.count { (it.stressLevel ?: 5) <= 2 }
            val rate = low.toFloat() / stress.size * 100f

            insights.add(ContextualInsight(
                "stress", "Stress Level",
                if (avg <= 2.5) "positive" else "negative",
                rate,
                when {
                    avg <= 2.0 -> "Low stress boosts consistency 😌"
                    avg <= 3.5 -> "Resilient despite stress!"
                    else -> "Stress disrupts this habit"
                }
            ))
        }

        // Location
        val loc = checkIns.filter { !it.location.isNullOrBlank() }
        if (loc.isNotEmpty()) {
            val best = loc.groupBy { it.location }.mapValues { it.value.size }.maxByOrNull { it.value }
            best?.let {
                val rate = it.value.toFloat() / loc.size * 100f
                insights.add(ContextualInsight(
                    "location", "Location", "positive", rate,
                    "Most successful at ${it.key} (${String.format("%.0f", rate)}%) 📍"
                ))
            }
        }

        return insights
    }

    private fun findOptimalConditions(checkIns: List<CheckIn>): Map<String, String> {
        val conditions = mutableMapOf<String, String>()

        checkIns.filter { !it.mood.isNullOrBlank() }
            .groupBy { it.mood }.mapValues { it.value.size }.maxByOrNull { it.value }
            ?.let { conditions["Mood"] = "${getMoodEmoji(it.key)} ${it.key?.replace("_", " ")}" }

        checkIns.groupBy { getTimeCategory(it.completedAt) }
            .mapValues { it.value.size }.maxByOrNull { it.value }
            ?.let { conditions["Time"] = it.key }

        val highEnergy = checkIns.count { (it.energyLevel ?: 0) >= 4 }
        if (highEnergy > 0) conditions["Energy"] = "High (4-5)"

        val lowStress = checkIns.count { (it.stressLevel ?: 5) <= 2 }
        if (lowStress > 0) conditions["Stress"] = "Low (1-2)"

        return conditions
    }

    private fun identifyRiskFactors(habitData: HabitWithCheckIns): List<String> {
        val risks = mutableListOf<String>()
        val checkIns = habitData.checkIns

        if (checkIns.isEmpty()) return risks

        val lateRate = checkIns.count { it.isLate }.toFloat() / checkIns.size
        if (lateRate > 0.5f) risks.add("⚠️ Late ${String.format("%.0f", lateRate * 100f)}% - earlier reminders")

        val lowEnergy = checkIns.count { (it.energyLevel ?: 3) <= 2 }.toFloat() / checkIns.size
        if (lowEnergy > 0.4f) risks.add("⚠️ Low energy affects this - try after rest")

        val highStress = checkIns.count { (it.stressLevel ?: 3) >= 4 }.toFloat() / checkIns.size
        if (highStress > 0.4f) risks.add("⚠️ Stress disrupts this - manage stress first")

        return risks
    }

    private fun calculateAdvancedScore(habitData: HabitWithCheckIns): Float {
        val checkIns = habitData.checkIns
        if (checkIns.isEmpty()) return 0f

        var score = 0f
        val punctual = checkIns.count { !it.isLate }.toFloat() / checkIns.size * 25f
        score += punctual

        val positive = checkIns.count { it.mood in listOf("happy", "very_happy") }
        if (positive > 0) score += positive.toFloat() / checkIns.size * 25f

        val highEnergy = checkIns.count { (it.energyLevel ?: 0) >= 4 }
        if (highEnergy > 0) score += highEnergy.toFloat() / checkIns.size * 25f

        score += calculateConsistency(habitData) * 25f

        return score.coerceIn(0f, 100f)
    }

    private fun generateAdvancedRecommendation(habitData: HabitWithCheckIns): String {
        val rec = mutableListOf<String>()
        val mood = analyzeMoodImpact(habitData.checkIns)
        val time = analyzeTimeImpact(habitData)

        if (mood.hasData && mood.bestMood != null) {
            rec.add("✨ Best when ${getMoodEmoji(mood.bestMood)}")
        }
        if (time.hasData && time.optimalTimeWindow != null) {
            rec.add("⏰ ${time.optimalTimeWindow}")
        }

        val risks = identifyRiskFactors(habitData)
        if (risks.isNotEmpty()) rec.add(risks.first())

        return rec.joinToString("\n").ifEmpty { "Keep tracking! 📊" }
    }

    private fun getTimeCategory(time: String): String {
        return try {
            val hour = time.split(":")[0].toInt()
            when {
                hour in 5..8 -> "Early Morning (5-8 AM)"
                hour in 9..11 -> "Late Morning (9-11 AM)"
                hour in 12..14 -> "Afternoon (12-2 PM)"
                hour in 15..17 -> "Late Afternoon (3-5 PM)"
                hour in 18..20 -> "Evening (6-8 PM)"
                hour in 21..23 -> "Night (9-11 PM)"
                else -> "Late Night"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getMoodEmoji(mood: String?): String {
        return when (mood) {
            "very_happy" -> "😄"
            "happy" -> "😊"
            "neutral" -> "😐"
            "sad" -> "😢"
            "very_sad" -> "😭"
            else -> "😐"
        }
    }
}
