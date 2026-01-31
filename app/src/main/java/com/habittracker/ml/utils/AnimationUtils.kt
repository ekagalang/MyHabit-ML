package com.habittracker.ml.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun rememberConfettiState(): ConfettiState {
    return remember { ConfettiState() }
}

class ConfettiState {
    var isActive by mutableStateOf(false)
        private set

    fun trigger() {
        isActive = true
    }

    fun reset() {
        isActive = false
    }
}

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Dp,
    val velocity: Float,
    val angle: Float,
    val rotation: Float
)

fun generateConfetti(count: Int = 50): List<ConfettiParticle> {
    val colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFF45B7D1),
        Color(0xFFFFA07A),
        Color(0xFF98D8C8),
        Color(0xFFF7DC6F),
        Color(0xFFBB8FCE),
        Color(0xFF85C1E2)
    )

    return List(count) {
        ConfettiParticle(
            x = Random.nextFloat(),
            y = -0.1f,
            color = colors.random(),
            size = Random.nextInt(4, 12).dp,
            velocity = Random.nextFloat() * 2f + 1f,
            angle = Random.nextFloat() * 360f,
            rotation = Random.nextFloat() * 360f
        )
    }
}

object AnimationConstants {
    const val FAST_DURATION = 150
    const val NORMAL_DURATION = 300
    const val SLOW_DURATION = 500

    val SPRING_BOUNCY = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val SPRING_SMOOTH = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}