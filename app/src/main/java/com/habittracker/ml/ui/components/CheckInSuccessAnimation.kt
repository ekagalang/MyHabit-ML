package com.habittracker.ml.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CheckInSuccessAnimation(
    habitName: String,
    onAnimationEnd: () -> Unit = {}
) {
    var progress by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Scale in
        animate(0f, 1f, animationSpec = tween(300, easing = FastOutSlowInEasing)) { value, _ ->
            scale = value
        }

        // Draw checkmark
        animate(0f, 1f, animationSpec = tween(500, easing = LinearEasing)) { value, _ ->
            progress = value
        }

        // Fade in text
        animate(0f, 1f, animationSpec = tween(300)) { value, _ ->
            alpha = value
        }

        delay(1500)
        onAnimationEnd()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Checkmark Circle
            Canvas(
                modifier = Modifier.size(120.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = size.minDimension / 2 * scale

                // Circle background
                drawCircle(
                    color = Color(0xFF4CAF50),
                    radius = radius,
                    center = Offset(centerX, centerY)
                )

                // Checkmark
                if (progress > 0f) {
                    val checkPath = Path().apply {
                        val startX = centerX - radius * 0.3f
                        val startY = centerY
                        val midX = centerX - radius * 0.1f
                        val midY = centerY + radius * 0.3f
                        val endX = centerX + radius * 0.4f
                        val endY = centerY - radius * 0.3f

                        moveTo(startX, startY)

                        // First part of checkmark
                        if (progress < 0.5f) {
                            val p = progress * 2
                            lineTo(
                                startX + (midX - startX) * p,
                                startY + (midY - startY) * p
                            )
                        } else {
                            lineTo(midX, midY)
                            // Second part of checkmark
                            val p = (progress - 0.5f) * 2
                            lineTo(
                                midX + (endX - midX) * p,
                                midY + (endY - midY) * p
                            )
                        }
                    }

                    drawPath(
                        path = checkPath,
                        color = Color.White,
                        style = Stroke(
                            width = 8.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }
            }

            // Success text
            if (alpha > 0f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Great Job! ✨",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = habitName,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                    )
                }
            }
        }
    }
}