package com.habittracker.ml.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.habittracker.ml.utils.rememberConfettiState

@Composable
fun StreakMilestoneDialog(
    streak: Int,
    habitName: String,
    onDismiss: () -> Unit
) {
    val confettiState = rememberConfettiState()
    var scale by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        confettiState.trigger()
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) { value, _ ->
            scale = value
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Confetti background
            ConfettiAnimation(state = confettiState)

            // Dialog content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .align(Alignment.Center)
                    .scale(scale),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Badge
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    streak >= 100 -> Color(0xFFFFD700) // Gold
                                    streak >= 30 -> Color(0xFFC0C0C0) // Silver
                                    else -> Color(0xFFCD7F32) // Bronze
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getMilestoneEmoji(streak),
                            fontSize = 48.sp
                        )
                    }

                    // Title
                    Text(
                        text = "Milestone Reached!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Streak count
                    Text(
                        text = "$streak Day Streak!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Message
                    Text(
                        text = getMilestoneMessage(streak, habitName),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    // Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Awesome!")
                    }
                }
            }
        }
    }
}

private fun getMilestoneEmoji(streak: Int): String {
    return when {
        streak >= 100 -> "🏆"
        streak >= 30 -> "🎖️"
        streak >= 7 -> "🌟"
        else -> "🎉"
    }
}

private fun getMilestoneMessage(streak: Int, habitName: String): String {
    return when {
        streak >= 100 -> "You've mastered $habitName! 100 days of dedication is incredible!"
        streak >= 30 -> "One month strong! $habitName is now part of your lifestyle!"
        streak >= 7 -> "One week streak! You're building momentum with $habitName!"
        else -> "Great start with $habitName! Keep it up!"
    }
}