package com.example.fluidz

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun OasisBackground(
    isSecondary: Boolean = false,
) {
    // Primary background (Main screen) - Lighter for airy feel
    val skyColor = Color(0xFFE3F2FD)
    val horizonColor = Color(0xFFBBDEFB)
    
    // Secondary background (Help, Settings, About) - Richer blue for focus
    val secondarySkyColor = Color(0xFF90CAF9)
    val secondaryHorizonColor = Color(0xFF64B5F6)

    val finalSkyColor = if (isSecondary) secondarySkyColor else skyColor
    val finalHorizonColor = if (isSecondary) secondaryHorizonColor else horizonColor
    val duneColor = Color(0xFFCC5500) // Burnt Orange

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(finalSkyColor, finalHorizonColor),
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Draw Dunes at the bottom in Burnt Orange
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, height * 0.7f)
                    quadraticBezierTo(
                        width * 0.25f, height * 0.65f,
                        width * 0.5f, height * 0.75f
                    )
                    quadraticBezierTo(
                        width * 0.75f, height * 0.85f,
                        width, height * 0.7f
                    )
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                },
                color = duneColor
            )
        }
    }
}
