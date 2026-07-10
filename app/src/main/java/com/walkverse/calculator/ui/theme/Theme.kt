package com.walkverse.calculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// iOS 26 Liquid Glass Color Tokens
object GlassTheme {
    val BackgroundDark = Color(0xFF070B19)
    
    // Glass surface base colors
    val GlassBase = Color.White.copy(alpha = 0.10f)
    val GlassHover = Color.White.copy(alpha = 0.18f)
    val GlassBorder = Color.White.copy(alpha = 0.20f)
    val GlassRing = Color.White.copy(alpha = 0.10f)
    
    // Glass keys variations
    val NumberKeyGlass = Color.White.copy(alpha = 0.08f)
    
    // Operator glow keys (blue/cyan accented)
    val OperatorKeyGlass = Color(0xFF3B82F6).copy(alpha = 0.15f)
    val OperatorGlow = Color(0xFF60A5FA)
    
    // Prominent equals key (highly luminous glass)
    val EqualsKeyGlassStart = Color(0xFF00E5FF).copy(alpha = 0.40f)
    val EqualsKeyGlassEnd = Color(0xFF2979FF).copy(alpha = 0.50f)
    val EqualsGlow = Color(0xFF00E5FF)
    
    // Typography color states
    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.70f)
    val TextMuted = Color.White.copy(alpha = 0.40f)
}

val GlassTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 64.sp,
        lineHeight = 72.sp,
        letterSpacing = (-0.5).sp,
        color = GlassTheme.TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = GlassTheme.TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = GlassTheme.TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = GlassTheme.TextPrimary
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = GlassTheme.TextSecondary
    )
)

@Composable
fun LiquidGlassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF3B82F6),
            secondary = Color(0xFF00E5FF),
            background = GlassTheme.BackgroundDark,
            surface = GlassTheme.GlassBase
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF2563EB),
            secondary = Color(0xFF00B8D4),
            background = Color(0xFF0A0F26),
            surface = GlassTheme.GlassBase
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GlassTypography,
        content = content
    )
}
