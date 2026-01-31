package com.habittracker.ml.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.entities.HabitWithCheckIns
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.ml.HabitPredictor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedInsightsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { HabitDatabase.getDatabase(context) }
    val repository = remember {
        HabitRepository(database.habitDao(), database.checkInDao(), database.habitTemplateDao())
    }
    val habitsWithCheckIns by produceState<List<HabitWithCheckIns>>(
        initialValue = emptyList(),
        repository
    ) {
        value = repository.getAllHabitsWithCheckIns()
    }
    val predictor = remember { HabitPredictor() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Insights") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (habitsWithCheckIns.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                items(habitsWithCheckIns) { habitData ->
                    val analysis = remember(habitData) {
                        predictor.analyzeHabitAdvanced(habitData)
                    }

                    AdvancedHabitCard(analysis)
                }
            }
        }
    }
}

@Composable
fun AdvancedHabitCard(analysis: com.habittracker.ml.ml.models.AdvancedPrediction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = analysis.habitName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Score Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                analysis.overallSuccessScore >= 80f -> MaterialTheme.colorScheme.primaryContainer
                                analysis.overallSuccessScore >= 60f -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.errorContainer
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${analysis.overallSuccessScore.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider()

            // Mood Correlation
            if (analysis.moodCorrelation.hasData) {
                InsightSection(
                    icon = Icons.Default.Mood,
                    title = "Mood Impact",
                    content = analysis.moodCorrelation.insight
                )
            }

            // Time Correlation
            if (analysis.timeCorrelation.hasData) {
                InsightSection(
                    icon = Icons.Default.Schedule,
                    title = "Timing Analysis",
                    content = analysis.timeCorrelation.insight
                )
            }

            // Contextual Insights
            if (analysis.contextualInsights.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = "Key Factors",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                analysis.contextualInsights.forEach { insight ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (insight.type) {
                                "energy" -> Icons.Default.Bolt
                                "stress" -> Icons.Default.Psychology
                                "location" -> Icons.Default.LocationOn
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = when (insight.impact) {
                                "positive" -> MaterialTheme.colorScheme.primary
                                "negative" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = insight.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Optimal Conditions
            if (analysis.optimalConditions.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = "🎯 Best Conditions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                analysis.optimalConditions.forEach { (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Risk Factors
            if (analysis.riskFactors.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = "⚠️ Watch Out For",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )

                analysis.riskFactors.forEach { risk ->
                    Text(
                        text = risk,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            // Recommendation
            if (analysis.recommendation.isNotBlank()) {
                HorizontalDivider()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "💡 Smart Recommendation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = analysis.recommendation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InsightSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No Data Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Start tracking your habits with mood, energy, and context to unlock advanced insights!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
