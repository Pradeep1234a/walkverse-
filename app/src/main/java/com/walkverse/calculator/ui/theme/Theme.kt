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
    val BackgroundDark = Color(0xFF03050C)
    
    // Glass surface base colors
    val GlassBase = Color.White.copy(alpha = 0.10f)
    val GlassHover = Color.White.copy(alpha = 0.22f)
    val GlassBorder = Color.White.copy(alpha = 0.25f)
    
    // Glass keys variations (15% dark translucent glass)
    val NumberKeyGlass = Color(0xFF2C2C2E).copy(alpha = 0.45f)
    val NumberKeyHighlight = Color.White.copy(alpha = 0.15f)
    
    // Function keys (Frosted light blue/grey glass)
    val FunctionKeyGlass = Color(0xFF8E8E93).copy(alpha = 0.35f)
    val FunctionText = Color.White
    
    // Operator keys (Amber/Orange Liquid Glass)
    val OperatorKeyGlass = Color(0xFFFF9F0A).copy(alpha = 0.65f)
    val OperatorGlow = Color(0xFFFFB84D)
    val OperatorText = Color.White
    
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
            primary = Color(0xFFFF9F0A),
            secondary = Color(0xFFFFB84D),
            background = GlassTheme.BackgroundDark,
            surface = GlassTheme.GlassBase
        )
    } else {
        lightColorScheme(
            primary = Color(0xFFD97706),
            secondary = Color(0xFFF59E0B),
            background = Color(0xFF03050C),
            surface = GlassTheme.GlassBase
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GlassTypography,
        content = content
    )
}
