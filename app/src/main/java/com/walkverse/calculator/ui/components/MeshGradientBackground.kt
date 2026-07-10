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
    // Setting up three drifting animations for the mesh nodes
    val infiniteTransition = rememberInfiniteTransition(label = "MeshTransition")

    val t1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TimeNode1"
    )

    val t2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TimeNode2"
    )

    val t3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TimeNode3"
    )

    // Base colors matching user choices
    val (color1, color2, color3) = when (themeName) {
        "mesh_aurora" -> Triple(
            Color(0xFF00B8D4), // Cyan
            Color(0xFF0D47A1), // Deep Blue
            Color(0xFF00E676)  // Greenish Teal
        )
        "mesh_sunset" -> Triple(
            Color(0xFFD500F9), // Purple
            Color(0xFFFF3D00), // Bright Orange
            Color(0xFFFF8F00)  // Deep Amber
        )
        else -> Triple( // "mesh_nebula" (Default)
            Color(0xFF7C4DFF), // Royal Violet
            Color(0xFF00E5FF), // Cyan
            Color(0xFF651FFF)  // Purple Blue
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(GlassTheme.BackgroundDark)
    ) {
        val width = size.width
        val height = size.height

        // Calculate animated node positions using trig functions
        val node1X = width * (0.3f + 0.15f * sin(t1))
        val node1Y = height * (0.25f + 0.12f * cos(t1))
        val radius1 = width * (0.75f + 0.05f * sin(t2))

        val node2X = width * (0.7f + 0.15f * cos(t2))
        val node2Y = height * (0.6f + 0.15f * sin(t2))
        val radius2 = width * (0.85f + 0.08f * cos(t3))

        val node3X = width * (0.45f + 0.2f * sin(t3))
        val node3Y = height * (0.75f + 0.1f * cos(t1))
        val radius3 = width * (0.7f + 0.06f * sin(t2))

        // Draw Deep Blue Background Base Layer
        drawRect(
            color = GlassTheme.BackgroundDark
        )

        // Draw first mesh blob (Violet/Purple)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(node1X, node1Y),
                radius = radius1
            ),
            center = Offset(node1X, node1Y),
            radius = radius1
        )

        // Draw second mesh blob (Cyan)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color2.copy(alpha = 0.38f), Color.Transparent),
                center = Offset(node2X, node2Y),
                radius = radius2
            ),
            center = Offset(node2X, node2Y),
            radius = radius2
        )

        // Draw third mesh blob (Accent color)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color3.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(node3X, node3Y),
                radius = radius3
            ),
            center = Offset(node3X, node3Y),
            radius = radius3
        )
    }
}
