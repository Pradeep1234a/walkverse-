package com.walkverse.calculator.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walkverse.calculator.ui.theme.GlassTheme
import com.walkverse.calculator.ui.theme.SuperellipseShape
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

enum class GlassButtonType {
    NUMBER,
    FUNCTION,
    OPERATOR,
    EQUALS
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: GlassButtonType = GlassButtonType.NUMBER,
    isWide: Boolean = false,
    isActiveOperator: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(80),
        label = "ButtonScale"
    )

    val superellipse = SuperellipseShape(exponent = 4.8f)

    val infiniteTransition = rememberInfiniteTransition(label = "SpecularReflection")
    val reflectionProgress by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ReflectionProgress"
    )

    val baseColor = when (type) {
        GlassButtonType.NUMBER -> {
            if (isPressed) Color.White.copy(alpha = 0.12f) else GlassTheme.NumberKeyGlass
        }
        GlassButtonType.FUNCTION -> {
            if (isPressed) Color.White.copy(alpha = 0.18f) else GlassTheme.FunctionKeyGlass
        }
        GlassButtonType.OPERATOR -> {
            if (isActiveOperator) {
                Color.White.copy(alpha = 0.90f)
            } else if (isPressed) {
                Color(0xFF0A84FF).copy(alpha = 0.25f)
            } else {
                GlassTheme.OperatorKeyGlass
            }
        }
        GlassButtonType.EQUALS -> {
            if (isPressed) Color(0xFF0A84FF).copy(alpha = 0.28f) else Color(0xFF0A84FF).copy(alpha = 0.20f)
        }
    }

    val textColor = when (type) {
        GlassButtonType.OPERATOR -> {
            if (isActiveOperator) Color(0xFF0A84FF) else Color.White
        }
        else -> Color.White
    }

    val textFontSize = if (text.length > 2) 20.sp else 28.sp
    val textFontWeight = if (type == GlassButtonType.NUMBER) FontWeight.Normal else FontWeight.Medium

    Box(
        modifier = modifier
            .scale(scale)
            .then(
                if (isWide) Modifier.fillMaxWidth() else Modifier.aspectRatio(1f)
            )
            .then(
                if (type == GlassButtonType.EQUALS) {
                    Modifier.shadow(
                        elevation = 14.dp,
                        shape = superellipse,
                        clip = false,
                        ambientColor = GlassTheme.OperatorGlow,
                        spotColor = GlassTheme.OperatorGlow
                    )
                } else if (isActiveOperator) {
                    Modifier.shadow(
                        elevation = 10.dp,
                        shape = superellipse,
                        clip = false,
                        ambientColor = Color.White,
                        spotColor = Color.White
                    )
                } else {
                    Modifier
                }
            )
            .clip(superellipse)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = if (isWide) Alignment.CenterStart else Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val steps = 120
            val n = 4.8

            val superellipsePath = Path().apply {
                val cx = width / 2f
                val cy = height / 2f
                val rx = width / 2f
                val ry = height / 2f
                for (i in 0..steps) {
                    val theta = i * 2.0 * Math.PI / steps
                    val cosT = cos(theta)
                    val sinT = sin(theta)
                    val xSign = if (cosT >= 0.0) 1.0 else -1.0
                    val ySign = if (sinT >= 0.0) 1.0 else -1.0
                    val x = cx + rx * xSign * abs(cosT).pow(2.0 / n)
                    val y = cy + ry * ySign * abs(sinT).pow(2.0 / n)
                    if (i == 0) moveTo(x.toFloat(), y.toFloat()) else lineTo(x.toFloat(), y.toFloat())
                }
                close()
            }

            // 1. Transparent Base Layer
            drawPath(
                path = superellipsePath,
                color = baseColor
            )

            // 2. Refraction Layer (1.8dp shift)
            clipPath(superellipsePath) {
                drawPath(
                    path = superellipsePath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.45f + 1.8.dp.toPx(), height * 0.45f + 1.8.dp.toPx()),
                        radius = width * 0.5f
                    )
                )
            }

            // 3. Internal Light Layer
            clipPath(superellipsePath) {
                val glowColor = when (type) {
                    GlassButtonType.OPERATOR -> Color(0xFF0A84FF).copy(alpha = 0.12f)
                    GlassButtonType.NUMBER -> Color(0xFF7C4DFF).copy(alpha = 0.06f)
                    else -> Color.White.copy(alpha = 0.05f)
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor, Color.Transparent),
                        center = Offset(width * 0.5f, height * 0.5f),
                        radius = width * 0.6f
                    ),
                    center = Offset(width * 0.5f, height * 0.5f),
                    radius = width * 0.6f
                )
            }

            // 4. Edge Highlight Layer (Double-rim outline)
            // Outer Rim (35% opacity)
            drawPath(
                path = superellipsePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.10f)
                    )
                ),
                style = Stroke(width = 1.dp.toPx())
            )
            // Inner Rim (65% top, 25% bottom, 80% top highlight)
            drawPath(
                path = superellipsePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.80f),
                        Color.White.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.25f)
                    ),
                    startY = 0f,
                    endY = height
                ),
                style = Stroke(width = 1.8.dp.toPx())
            )

            // 5. Specular Glare Layer
            if (!isActiveOperator) {
                clipPath(superellipsePath) {
                    val startX = width * reflectionProgress
                    val endX = startX + width * 0.5f
                    val strokeW = 18.dp.toPx()

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.14f),
                                Color.White.copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            start = Offset(startX, 0f),
                            end = Offset(endX, height)
                        ),
                        start = Offset(startX, 0f),
                        end = Offset(endX, height),
                        strokeWidth = strokeW
                    )

                    val secStartX = startX - width * 0.22f
                    val secEndX = secStartX + width * 0.5f
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            start = Offset(secStartX, 0f),
                            end = Offset(secEndX, height)
                        ),
                        start = Offset(secStartX, 0f),
                        end = Offset(secEndX, height),
                        strokeWidth = 6.dp.toPx()
                    )
                }
            }

            // 6. Specular Hotspot
            if (!isActiveOperator) {
                clipPath(superellipsePath) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.32f),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.25f, height * 0.25f),
                            radius = width * 0.18f
                        ),
                        center = Offset(width * 0.25f, height * 0.25f),
                        radius = width * 0.18f
                    )
                }
            }
        }

        Text(
            text = text,
            color = textColor,
            fontSize = textFontSize,
            fontWeight = textFontWeight,
            modifier = if (isWide) Modifier.padding(start = 32.dp) else Modifier
        )
    }
}
