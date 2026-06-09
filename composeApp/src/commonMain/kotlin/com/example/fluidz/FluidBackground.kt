package com.example.fluidz

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun FluidBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "fluid")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    // Using softer colors to ensure black text is readable
    val waveColor1 = Color(0xFFCC5500) // Burnt Orange
    val waveColor2 = Color(0xFF003366) // Dark Blue
    val bgColor = Color(0xFFF5F5F5)   // Light Grey Background

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(color = bgColor)

        val path = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val y = height * 0.7f + sin(x * 0.01f + phase) * 50f
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(waveColor1, waveColor1.copy(alpha = 0.6f)),
            )
        )

        val path2 = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val y = height * 0.75f + sin(x * 0.008f + phase * 0.8f + 1f) * 40f
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }

        drawPath(
            path = path2,
            brush = Brush.verticalGradient(
                colors = listOf(waveColor2.copy(alpha = 0.4f), waveColor1.copy(alpha = 0.4f))
            )
        )
    }
}
