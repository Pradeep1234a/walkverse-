package com.liquidglass.calculator.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.calculator.ui.theme.GlassTheme

enum class GlassButtonType {
    NUMBER,
    OPERATOR,
    EQUALS,
    FUNCTION
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: GlassButtonType = GlassButtonType.NUMBER,
    isActiveOperator: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Scale compression on click
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(100),
        label = "ButtonScale"
    )

    // Opacity changes based on press state
    val baseColor = when (type) {
        GlassButtonType.NUMBER -> {
            if (isPressed) GlassTheme.GlassHover else GlassTheme.NumberKeyGlass
        }
        GlassButtonType.FUNCTION -> {
            if (isPressed) GlassTheme.GlassHover else GlassTheme.GlassBase
        }
        GlassButtonType.OPERATOR -> {
            if (isActiveOperator) {
                GlassTheme.OperatorGlow.copy(alpha = 0.35f)
            } else if (isPressed) {
                GlassTheme.OperatorKeyGlass.copy(alpha = 0.28f)
            } else {
                GlassTheme.OperatorKeyGlass
            }
        }
        GlassButtonType.EQUALS -> Color.Transparent // Will use gradient instead
    }

    val borderColors = when (type) {
        GlassButtonType.EQUALS -> listOf(
            Color.White.copy(alpha = 0.60f),
            Color.White.copy(alpha = 0.20f)
        )
        GlassButtonType.OPERATOR -> {
            if (isActiveOperator) {
                listOf(GlassTheme.OperatorGlow.copy(alpha = 0.6f), GlassTheme.OperatorGlow.copy(alpha = 0.2f))
            } else {
                listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
            }
        }
        else -> listOf(
            Color.White.copy(alpha = 0.20f),
            Color.White.copy(alpha = 0.05f)
        )
    }

    val textFontWeight = if (type == GlassButtonType.NUMBER) FontWeight.Normal else FontWeight.Medium
    val textFontSize = if (text.length > 2) 18.sp else 24.sp
    
    val textColor = when (type) {
        GlassButtonType.EQUALS -> Color.White
        GlassButtonType.OPERATOR -> if (isActiveOperator) Color.White else GlassTheme.OperatorGlow
        GlassButtonType.FUNCTION -> GlassTheme.TextSecondary
        GlassButtonType.NUMBER -> Color.White
    }

    Box(
        modifier = modifier
            .scale(scale)
            .aspectRatio(1f) // Ensure perfect circular aspect ratio
            .then(
                if (type == GlassButtonType.EQUALS) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = GlassTheme.EqualsGlow,
                        spotColor = GlassTheme.EqualsGlow
                    )
                } else if (isActiveOperator) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = GlassTheme.OperatorGlow,
                        spotColor = GlassTheme.OperatorGlow
                    )
                } else {
                    Modifier
                }
            )
            .clip(CircleShape)
            .then(
                if (type == GlassButtonType.EQUALS) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                GlassTheme.EqualsKeyGlassStart,
                                GlassTheme.EqualsKeyGlassEnd
                            )
                        )
                    )
                } else {
                    Modifier.background(baseColor)
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(colors = borderColors),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disable default Android ripple in favor of glass compression
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = textFontSize,
            fontWeight = textFontWeight
        )
    }
}
