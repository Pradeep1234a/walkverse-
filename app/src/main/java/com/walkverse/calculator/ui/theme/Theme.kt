package com.walkverse.calculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

// Mathematical Superellipse Shape (Lame Curve) with exponent n = 4.8
class SuperellipseShape(private val exponent: Float = 4.8f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rx = size.width / 2f
        val ry = size.height / 2f
        
        val n = exponent.toDouble()
        val steps = 120
        for (i in 0..steps) {
            val theta = i * 2.0 * Math.PI / steps
            val cosT = cos(theta)
            val sinT = sin(theta)
            
            val xSign = if (cosT >= 0.0) 1.0 else -1.0
            val ySign = if (sinT >= 0.0) 1.0 else -1.0
            
            val x = cx + rx * xSign * abs(cosT).pow(2.0 / n)
            val y = cy + ry * ySign * abs(sinT).pow(2.0 / n)
            
            if (i == 0) {
                path.moveTo(x.toFloat(), y.toFloat())
            } else {
                path.lineTo(x.toFloat(), y.toFloat())
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}

// iOS 26 Optical Liquid Glass Color Tokens
object GlassTheme {
    val BackgroundDark = Color(0xFF03050C)
    
    // Transparent Card Base (8% - 12% alpha)
    val CardGlassBase = Color.White.copy(alpha = 0.10f)
    
    // Transparent Numbers (5% - 8% alpha)
    val NumberKeyGlass = Color(0xFF2C2C2E).copy(alpha = 0.06f)
    
    // Transparent Operators (12% - 18% alpha blue-cyan)
    val OperatorKeyGlass = Color(0xFF0A84FF).copy(alpha = 0.15f)
    
    // Transparent Function Keys (AC, ±, %) (10% alpha)
    val FunctionKeyGlass = Color(0xFF8E8E93).copy(alpha = 0.10f)
    
    // Glow accents
    val OperatorGlow = Color(0xFF0A84FF)
    
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
            primary = Color(0xFF0A84FF),
            secondary = Color(0xFF64B5F6),
            background = GlassTheme.BackgroundDark,
            surface = GlassTheme.CardGlassBase
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF007AFF),
            secondary = Color(0xFF1E88E5),
            background = Color(0xFF03050C),
            surface = GlassTheme.CardGlassBase
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GlassTypography,
        content = content
    )
}
