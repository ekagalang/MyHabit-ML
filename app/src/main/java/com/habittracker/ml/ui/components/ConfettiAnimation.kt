package com.habittracker.ml.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import com.habittracker.ml.utils.ConfettiState
import com.habittracker.ml.utils.generateConfetti
import kotlinx.coroutines.delay

@Composable
fun ConfettiAnimation(
    state: ConfettiState,
    modifier: Modifier = Modifier
) {
    val particles = remember { generateConfetti(50) }
    var animationProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(state.isActive) {
        if (state.isActive) {
            animationProgress = 0f
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(3000, easing = LinearEasing)
            ) { value, _ ->
                animationProgress = value
            }
            delay(100)
            state.reset()
        }
    }

    if (state.isActive || animationProgress > 0f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            particles.forEach { particle ->
                val currentY = particle.y + (particle.velocity * animationProgress * canvasHeight)
                val currentX = particle.x * canvasWidth +
                        (kotlin.math.sin(animationProgress * particle.angle) * 50)

                if (currentY < canvasHeight) {
                    rotate(
                        degrees = particle.rotation * animationProgress * 360f,
                        pivot = Offset(currentX, currentY)
                    ) {
                        drawRect(
                            color = particle.color,
                            topLeft = Offset(
                                currentX - particle.size.toPx() / 2,
                                currentY
                            ),
                            size = Size(particle.size.toPx(), particle.size.toPx())
                        )
                    }
                }
            }
        }
    }
}