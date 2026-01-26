package com.habittracker.ml.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.ui.theme.*
import com.habittracker.ml.utils.StreakCalculator
import kotlin.math.min

data class HabitPrediction(
    val habit: Habit,
    val currentStreak: Int,
    val completionRate: Float,
    val predictedScore: Int,
    val trend: String, // "improving", "stable", "declining"
    val recommendation: String
)

data class HabitCorrelation(
    val habit1: Habit,
    val habit2: Habit,
    val strength: Float // 0.0 to 1.0
)

@Composable
fun MLInsightsScreen(
    onNavigateBack: () -> Unit = {},
    showBack: Boolean = true,
    onNavigateToHabitDetail: (Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var habitCheckInsMap by remember { mutableStateOf<Map<Long, List<CheckIn>>>(emptyMap()) }
    var allCheckIns by remember { mutableStateOf<List<CheckIn>>(emptyList()) }

    LaunchedEffect(uiState.habits, uiState.todayCheckIns) {
        val allHabitsWithCheckIns = viewModel.getAllHabitsWithCheckIns()
        habitCheckInsMap = allHabitsWithCheckIns.associate { it.habit.id to it.checkIns }
        allCheckIns = allHabitsWithCheckIns.flatMap { it.checkIns }
    }

    // Calculate predictions
    val predictions = remember(uiState.habits, habitCheckInsMap) {
        uiState.habits.map { habit ->
            val checkIns = habitCheckInsMap[habit.id].orEmpty()
            val currentStreak = StreakCalculator.calculateCurrentStreak(checkIns)
            val completionRate = StreakCalculator.calculateCompletionRate(
                checkIns, habit.targetFrequency, 30
            )

            val predictedScore = min(95, (completionRate * 100 + currentStreak * 2).toInt())

            val trend = when {
                completionRate >= 0.8f -> "improving"
                completionRate >= 0.5f -> "stable"
                else -> "declining"
            }

            val recommendation = when (trend) {
                "improving" -> "Keep up the excellent work! You're on track."
                "stable" -> "Good consistency! Try to increase frequency."
                else -> "Need more focus. Set smaller goals to rebuild."
            }

            HabitPrediction(
                habit = habit,
                currentStreak = currentStreak,
                completionRate = completionRate,
                predictedScore = predictedScore,
                trend = trend,
                recommendation = recommendation
            )
        }.sortedByDescending { it.predictedScore }
    }

    // Calculate correlations
    val correlations = remember(uiState.habits, habitCheckInsMap) {
        val habitPairs = mutableListOf<HabitCorrelation>()

        for (i in uiState.habits.indices) {
            for (j in i + 1 until uiState.habits.size) {
                val habit1 = uiState.habits[i]
                val habit2 = uiState.habits[j]

                val checkIns1 = habitCheckInsMap[habit1.id].orEmpty()
                val checkIns2 = habitCheckInsMap[habit2.id].orEmpty()

                // Simple correlation: how many days both were done
                val dates1 = checkIns1.map { it.date }.toSet()
                val dates2 = checkIns2.map { it.date }.toSet()
                val intersection = dates1.intersect(dates2).size
                val union = dates1.union(dates2).size

                if (union > 0) {
                    val strength = intersection.toFloat() / union.toFloat()
                    if (strength > 0.3f) {
                        habitPairs.add(
                            HabitCorrelation(habit1, habit2, strength)
                        )
                    }
                }
            }
        }

        habitPairs.sortedByDescending { it.strength }
    }

    // Calculate overall score
    val overallScore = predictions.map { it.predictedScore }.average().toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BackgroundLight,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showBack) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextMain,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    } else {
                        Box(modifier = Modifier.size(40.dp))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ML Insights",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                        Text(
                            text = "AI-Powered Analysis",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Box(modifier = Modifier.size(40.dp))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Overall Score Card
        item {
            OverallScoreCard(
                score = overallScore,
                totalHabits = predictions.size
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Future Self Section
        item {
            SectionHeader(
                icon = Icons.Default.Star,
                title = "Future Self Predictions",
                iconColor = Highlight
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Predictions List
        items(predictions.take(5)) { prediction ->
            PredictionCard(
                prediction = prediction,
                onClick = { onNavigateToHabitDetail(prediction.habit.id) }
            )
        }

        if (predictions.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = "🤖",
                    title = "No Data Yet",
                    subtitle = "Start building habits to see ML predictions"
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Correlations Section
        if (correlations.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Default.DateRange,
                    title = "Habit Correlations",
                    iconColor = Primary
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                CorrelationsCard(
                    correlations = correlations.take(3)
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Best Times Section
        item {
            SectionHeader(
                icon = Icons.Default.DateRange,
                title = "Best Times Analysis",
                iconColor = Color(0xFF10B981)
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            BestTimesCard(
                checkIns = allCheckIns
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Insights Cards
        item {
            SectionHeader(
                icon = Icons.Default.Star,
                title = "Key Insights",
                iconColor = Color(0xFFF59E0B)
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            InsightsGrid(
                predictions = predictions
            )
        }
    }
}

@Composable
fun OverallScoreCard(
    score: Int,
    totalHabits: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1E293B),
        shadowElevation = 4.dp
    ) {
        Box {
            // Animated background circles
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = (-50).dp, y = (-50).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Primary.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = 250.dp, y = 100.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Highlight.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Primary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "✨ AI ANALYSIS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Score Circle
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(140.dp)
                    ) {}

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = score.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 56.sp
                        )
                        Text(
                            text = "SUCCESS SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Based on $totalHabits habits analyzed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        score >= 80 -> "🎉 Excellent! You're building strong habits."
                        score >= 60 -> "👍 Good progress! Keep pushing forward."
                        score >= 40 -> "💪 Building momentum. Stay consistent."
                        else -> "🌱 Starting fresh. Every day is progress."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PredictionCard(
    prediction: HabitPrediction,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = prediction.habit.icon,
                        fontSize = 32.sp
                    )

                    Column {
                        Text(
                            text = prediction.habit.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = getCategoryColor(prediction.habit.category).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = prediction.habit.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = getCategoryColor(prediction.habit.category),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }

                // Score badge
                Surface(
                    shape = CircleShape,
                    color = when {
                        prediction.predictedScore >= 80 -> Color(0xFF10B981).copy(alpha = 0.1f)
                        prediction.predictedScore >= 60 -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                        else -> AccentError.copy(alpha = 0.1f)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${prediction.predictedScore}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                prediction.predictedScore >= 80 -> Color(0xFF10B981)
                                prediction.predictedScore >= 60 -> Color(0xFFF59E0B)
                                else -> AccentError
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItem(
                    icon = "🔥",
                    value = prediction.currentStreak.toString(),
                    label = "Streak"
                )

                StatItem(
                    icon = "📊",
                    value = "${(prediction.completionRate * 100).toInt()}%",
                    label = "Rate"
                )

                StatItem(
                    icon = when (prediction.trend) {
                        "improving" -> "📈"
                        "stable" -> "→"
                        else -> "📉"
                    },
                    value = prediction.trend.replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase() else char.toString()
                    },
                    label = "Trend"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = BorderLight)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Highlight,
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = prediction.recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TextMain
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
fun CorrelationsCard(
    correlations: List<HabitCorrelation>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "These habits are often done together:",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            correlations.forEach { correlation ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = correlation.habit1.icon, fontSize = 24.sp)

                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(text = correlation.habit2.icon, fontSize = 24.sp)

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${correlation.habit1.name} + ${correlation.habit2.name}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextMain
                        )

                        LinearProgressIndicator(
                            progress = { correlation.strength },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Primary,
                            trackColor = BorderLight
                        )
                    }

                    Text(
                        text = "${(correlation.strength * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                if (correlation != correlations.last()) {
                    HorizontalDivider(
                        color = BorderLight,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BestTimesCard(
    checkIns: List<com.habittracker.ml.data.local.entities.CheckIn>
) {
    // Group check-ins by hour
    val hourCounts = checkIns
        .mapNotNull { checkIn ->
            checkIn.completedAt.split(":").firstOrNull()?.toIntOrNull()
        }
        .groupingBy { it }
        .eachCount()

    val bestHour = hourCounts.maxByOrNull { it.value }?.key ?: 8

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "⏰", fontSize = 24.sp)
                    }
                }

                Column {
                    Text(
                        text = "Your Peak Hour",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                    Text(
                        text = "${bestHour}:00 - ${bestHour + 1}:00",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You complete most habits around ${bestHour}:00. Try scheduling new habits at this time for better success!",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Simple hour distribution
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (6..22 step 4).forEach { hour ->
                    val count = hourCounts[hour] ?: 0
                    val maxCount = hourCounts.maxOfOrNull { it.value } ?: 1

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(60.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height((60 * (count.toFloat() / maxCount.toFloat())).dp)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        if (hour == bestHour)
                                            Color(0xFF10B981)
                                        else
                                            Color(0xFFE5E7EB)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${hour}h",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InsightsGrid(
    predictions: List<HabitPrediction>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val improving = predictions.count { it.trend == "improving" }
        val stable = predictions.count { it.trend == "stable" }
        val declining = predictions.count { it.trend == "declining" }

        InsightCard(
            icon = "📈",
            title = "Improving Habits",
            value = improving.toString(),
            subtitle = "Great momentum!",
            color = Color(0xFF10B981)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InsightCard(
                icon = "→",
                title = "Stable",
                value = stable.toString(),
                subtitle = "Keep going",
                color = Color(0xFF6366F1),
                modifier = Modifier.weight(1f)
            )

            InsightCard(
                icon = "📉",
                title = "Need Focus",
                value = declining.toString(),
                subtitle = "Rebuild momentum",
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun InsightCard(
    icon: String,
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 24.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: String,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
