package com.walkverse.calculator.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.walkverse.calculator.ui.theme.GlassTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MeshGradientBackground(
    themeName: String,
    modifier: Modifier = Modifier
) {
    // Drifting animations for ambient lighting spots
    val infiniteTransition = rememberInfiniteTransition(label = "MeshTransition")

    val t1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TimeNode1"
    )

    val t2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TimeNode2"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(GlassTheme.BackgroundDark)
    ) {
        val width = size.width
        val height = size.height

        // Drifting center offsets for cyan/blue and purple/magenta spots
        val leftNodeX = width * (0.05f + 0.08f * sin(t1))
        val leftNodeY = height * (0.45f + 0.06f * cos(t1))
        val leftRadius = width * (0.75f + 0.03f * sin(t2))

        val rightNodeX = width * (0.95f + 0.08f * cos(t2))
        val rightNodeY = height * (0.6f + 0.08f * sin(t2))
        val rightRadius = width * (0.85f + 0.04f * cos(t1))

        // Draw solid dark background
        drawRect(color = GlassTheme.BackgroundDark)

        // Draw Left Glowing spot (Deep Blue/Cyan)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1E3A8A).copy(alpha = 0.40f), // Indigo Blue
                    Color(0xFF0EA5E9).copy(alpha = 0.25f), // Cyan Blue
                    Color.Transparent
                ),
                center = Offset(leftNodeX, leftNodeY),
                radius = leftRadius
            ),
            center = Offset(leftNodeX, leftNodeY),
            radius = leftRadius
        )

        // Draw Right Glowing spot (Purple/Magenta)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF701A75).copy(alpha = 0.35f), // Dark Magenta
                    Color(0xFF4C1D95).copy(alpha = 0.28f), // Deep Violet
                    Color.Transparent
                ),
                center = Offset(rightNodeX, rightNodeY),
                radius = rightRadius
            ),
            center = Offset(rightNodeX, rightNodeY),
            radius = rightRadius
        )
    }
}
