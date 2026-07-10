package com.walkverse.calculator.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walkverse.calculator.ui.theme.GlassTheme

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
    
    // Scale compression on touch
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(80),
        label = "ButtonScale"
    )

    // Layout shape: Pill-shaped for "0", otherwise perfect Circle
    val shape = if (isWide) RoundedCornerShape(100.dp) else CircleShape

    // Base background colors mimicking physical glass refraction
    val baseColor = when (type) {
        GlassButtonType.NUMBER -> {
            if (isPressed) GlassTheme.GlassHover else GlassTheme.NumberKeyGlass
        }
        GlassButtonType.FUNCTION -> {
            if (isPressed) GlassTheme.GlassHover else GlassTheme.FunctionKeyGlass
        }
        GlassButtonType.OPERATOR -> {
            if (isActiveOperator) {
                Color.White // Apple active state inverts color to white background
            } else if (isPressed) {
                GlassTheme.OperatorKeyGlass.copy(alpha = 0.85f)
            } else {
                GlassTheme.OperatorKeyGlass
            }
        }
        GlassButtonType.EQUALS -> {
            if (isPressed) GlassTheme.OperatorKeyGlass.copy(alpha = 0.9f) else GlassTheme.OperatorKeyGlass.copy(alpha = 0.8f)
        }
    }

    // Border highlights
    val borderColors = if (isActiveOperator) {
        listOf(GlassTheme.OperatorGlow, GlassTheme.OperatorGlow.copy(alpha = 0.5f))
    } else {
        listOf(Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0.05f))
    }

    // Typography styling
    val textFontWeight = if (type == GlassButtonType.NUMBER) FontWeight.Normal else FontWeight.Medium
    val textFontSize = if (text.length > 2) 20.sp else 28.sp
    
    val textColor = when (type) {
        GlassButtonType.OPERATOR -> {
            if (isActiveOperator) Color(0xFFFF9F0A) else GlassTheme.OperatorText
        }
        GlassButtonType.FUNCTION -> GlassTheme.FunctionText
        else -> Color.White
    }

    Box(
        modifier = modifier
            .scale(scale)
            .then(
                if (isWide) Modifier.fillMaxWidth() else Modifier.aspectRatio(1f)
            )
            .then(
                if (type == GlassButtonType.EQUALS) {
                    Modifier.shadow(
                        elevation = 16.dp,
                        shape = shape,
                        clip = false,
                        ambientColor = GlassTheme.OperatorGlow,
                        spotColor = GlassTheme.OperatorGlow
                    )
                } else if (isActiveOperator) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = shape,
                        clip = false,
                        ambientColor = Color.White,
                        spotColor = Color.White
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(baseColor)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(colors = borderColors),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom physical scale feedback instead
                onClick = onClick
            ),
        contentAlignment = if (isWide) Alignment.CenterStart else Alignment.Center
    ) {
        // 3D Crescent Glass Glare (Liquid reflection overlay)
        if (!isActiveOperator) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val clipPath = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = Rect(0f, 0f, size.width, size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(100.dp.toPx(), 100.dp.toPx())
                        )
                    )
                }
                // Clip reflection to button borders
                clipPath(clipPath) {
                    val glarePath = Path().apply {
                        val glareHeight = size.height * 0.4f
                        // Oval crescent shape
                        addOval(Rect(0f, -size.height * 0.1f, size.width, glareHeight))
                    }
                    drawPath(
                        path = glarePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.28f),
                                Color.White.copy(alpha = 0.01f)
                            )
                        )
                    )
                }
            }
        }

        // Text labels (Pill "0" button is left-padded; standard circular keys are centered)
        Text(
            text = text,
            color = textColor,
            fontSize = textFontSize,
            fontWeight = textFontWeight,
            modifier = if (isWide) Modifier.padding(start = 32.dp) else Modifier
        )
    }
}
