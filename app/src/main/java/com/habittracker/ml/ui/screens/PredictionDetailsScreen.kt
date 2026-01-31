package com.habittracker.ml.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.ml.ml.HabitPredictor
import com.habittracker.ml.ml.models.AdvancedPrediction
import com.habittracker.ml.ml.models.ContextualInsight
import com.habittracker.ml.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionDetailsScreen(
    habitId: Long,
    onNavigateBack: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    var prediction by remember { mutableStateOf<AdvancedPrediction?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(habitId) {
        val habitWithCheckIns = viewModel.getHabitWithCheckIns(habitId)
        if (habitWithCheckIns != null) {
            val predictor = HabitPredictor()
            prediction = predictor.analyzeHabitAdvanced(habitWithCheckIns)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ML Predictions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        prediction?.let {
                            Text(
                                text = it.habitName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (prediction == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextMuted
                    )
                    Text(
                        text = "Not enough data yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMuted
                    )
                    Text(
                        text = "Keep tracking to unlock ML insights!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overall Success Score
                item {
                    OverallScoreCard(prediction!!)
                }

                // AI Recommendation
                item {
                    AIRecommendationCard(prediction!!)
                }

                // Mood Correlation
                if (prediction!!.moodCorrelation.hasData) {
                    item {
                        SectionHeader("Mood Impact Analysis", Icons.Default.Mood)
                    }
                    item {
                        MoodCorrelationCard(prediction!!.moodCorrelation)
                    }
                }

                // Time Correlation
                if (prediction!!.timeCorrelation.hasData) {
                    item {
                        SectionHeader("Timing Analysis", Icons.Default.Schedule)
                    }
                    item {
                        TimeCorrelationCard(prediction!!.timeCorrelation)
                    }
                }

                // Contextual Insights
                if (prediction!!.contextualInsights.isNotEmpty()) {
                    item {
                        SectionHeader("Contextual Factors", Icons.Default.Insights)
                    }
                    items(prediction!!.contextualInsights) { insight ->
                        ContextualInsightCard(insight)
                    }
                }

                // Optimal Conditions
                if (prediction!!.optimalConditions.isNotEmpty()) {
                    item {
                        SectionHeader("Optimal Conditions", Icons.Default.AutoAwesome)
                    }
                    item {
                        OptimalConditionsCard(prediction!!.optimalConditions)
                    }
                }

                // Risk Factors
                if (prediction!!.riskFactors.isNotEmpty()) {
                    item {
                        SectionHeader("Risk Factors", Icons.Default.Warning)
                    }
                    item {
                        RiskFactorsCard(prediction!!.riskFactors)
                    }
                }
            }
        }
    }
}

@Composable
fun OverallScoreCard(prediction: AdvancedPrediction) {
    val score = prediction.overallSuccessScore.roundToInt()
    val color = when {
        score >= 80 -> Color(0xFF10B981)
        score >= 60 -> Color(0xFFF59E0B)
        score >= 40 -> Color(0xFFEC4899)
        else -> Color(0xFFEF4444)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Box {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "ML SUCCESS SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animated Score
                AnimatedScoreDisplay(score = score, color = color)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        score >= 80 -> "Excellent Performance"
                        score >= 60 -> "Good Progress"
                        score >= 40 -> "Building Momentum"
                        else -> "Starting Out"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Based on ${prediction.habitName} tracking data",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun AnimatedScoreDisplay(score: Int, color: Color) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "score"
    )

    Text(
        text = "$animatedScore",
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.Black,
        color = color,
        fontSize = 72.sp
    )
}

@Composable
fun AIRecommendationCard(prediction: AdvancedPrediction) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Recommendation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = prediction.recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMain,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextMain
        )
    }
}

@Composable
fun MoodCorrelationCard(moodCorrelation: com.habittracker.ml.ml.models.MoodCorrelation) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = moodCorrelation.insight,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextMain
            )

            Spacer(modifier = Modifier.height(8.dp))

            for ((mood, rate) in moodCorrelation.moodSuccessRates) {
                MoodRateBar(
                    mood = mood,
                    rate = rate,
                    isBest = mood == moodCorrelation.bestMood,
                    isWorst = mood == moodCorrelation.worstMood
                )
            }
        }
    }
}

@Composable
fun MoodRateBar(mood: String, rate: Float, isBest: Boolean, isWorst: Boolean) {
    val moodEmoji = when (mood) {
        "very_happy" -> "😄"
        "happy" -> "😊"
        "neutral" -> "😐"
        "sad" -> "😢"
        "very_sad" -> "😭"
        else -> "😐"
    }

    val moodLabel = mood.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val color = when {
        isBest -> Color(0xFF10B981)
        isWorst -> Color(0xFFEF4444)
        else -> Color(0xFF94A3B8)
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = moodEmoji, fontSize = 20.sp)
                Text(
                    text = moodLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isBest || isWorst) FontWeight.Bold else FontWeight.Normal,
                    color = TextMain
                )
            }
            Text(
                text = "${rate.roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        LinearProgressIndicator(
            progress = { rate / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun TimeCorrelationCard(timeCorrelation: com.habittracker.ml.ml.models.TimeCorrelation) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = timeCorrelation.insight,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextMain
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeStatBox(
                    label = "Punctuality",
                    value = "${timeCorrelation.punctualityScore.roundToInt()}%",
                    icon = Icons.Default.CheckCircle,
                    color = if (timeCorrelation.punctualityScore > 80) Color(0xFF10B981) else Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )

                TimeStatBox(
                    label = "Avg Delay",
                    value = "${timeCorrelation.averageDelay}m",
                    icon = Icons.Default.Schedule,
                    color = if (timeCorrelation.averageDelay < 30) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }

            timeCorrelation.optimalTimeWindow?.let { optimalTime ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Primary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Best Time Window",
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = optimalTime,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeStatBox(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
fun ContextualInsightCard(insight: ContextualInsight) {
    val icon = when (insight.type) {
        "energy" -> Icons.Default.BatteryChargingFull
        "stress" -> Icons.Default.SelfImprovement
        "location" -> Icons.Default.Place
        else -> Icons.Default.Info
    }

    val color = if (insight.impact == "positive") Color(0xFF10B981) else Color(0xFFF59E0B)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.factor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }

            Text(
                text = "${insight.correlation.roundToInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun OptimalConditionsCard(conditions: Map<String, String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF10B981).copy(alpha = 0.1f),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Your Sweet Spot",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )

            Text(
                text = "You perform best under these conditions:",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(4.dp))

            conditions.forEach { (key, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = key,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        modifier = Modifier.width(80.dp)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun RiskFactorsCard(risks: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFEF3C7),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Watch Out For",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706)
                )
            }

            risks.forEach { risk ->
                Text(
                    text = risk,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF92400E)
                )
            }
        }
    }
}
