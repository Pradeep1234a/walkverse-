package com.walkverse.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Custom Shadcn Color Definitions
data class ShadcnColors(
    val primary: Color,
    val background: Color,
    val surface: Color,
    val border: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val cardGradStart: Color,
    val cardGradEnd: Color
)

val LocalShadcnColors = staticCompositionLocalOf {
    ShadcnColors(
        primary = Color.White,
        background = Color(0xFF09090B),
        surface = Color(0xFF18181B),
        border = Color(0xFF27272A),
        textPrimary = Color(0xFFF4F4F5),
        textMuted = Color(0xFFA1A1AA),
        accent = Color(0xFF3B82F6),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFF1E1B4B),
        cardGradEnd = Color(0xFF311042)
    )
}

// Balanced Custom Theme Palettes using neutral foundations and accent highlights
object WalkVerseThemePalettes {
    // 1. ZINC (Minimalist Slate/White)
    val ZincDark = ShadcnColors(
        primary = Color(0xFFFAFAFA),
        background = Color(0xFF09090B),
        surface = Color(0xFF18181B),
        border = Color(0xFF27272A),
        textPrimary = Color(0xFFF4F4F5),
        textMuted = Color(0xFFA1A1AA),
        accent = Color(0xFFFAFAFA),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFF18181B),
        cardGradEnd = Color(0xFF27272A)
    )
    val ZincLight = ShadcnColors(
        primary = Color(0xFF18181B),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF4F4F5),
        border = Color(0xFFE4E4E7),
        textPrimary = Color(0xFF09090B),
        textMuted = Color(0xFF71717A),
        accent = Color(0xFF18181B),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFFF4F4F5),
        cardGradEnd = Color(0xFFE4E4E7)
    )

    // 2. ROSE (Energetic Warm Cherry)
    val RoseDark = ShadcnColors(
        primary = Color(0xFFF43F5E),
        background = Color(0xFF09090B),
        surface = Color(0xFF18181B),
        border = Color(0xFF27272A),
        textPrimary = Color(0xFFF4F4F5),
        textMuted = Color(0xFFA1A1AA),
        accent = Color(0xFFF43F5E),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFF2D1016),
        cardGradEnd = Color(0xFF18181B)
    )
    val RoseLight = ShadcnColors(
        primary = Color(0xFFE11D48),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF4F4F5),
        border = Color(0xFFE4E4E7),
        textPrimary = Color(0xFF09090B),
        textMuted = Color(0xFF71717A),
        accent = Color(0xFFE11D48),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFFFFF1F2),
        cardGradEnd = Color(0xFFF4F4F5)
    )

    // 3. EMERALD (Nature & Growth)
    val EmeraldDark = ShadcnColors(
        primary = Color(0xFF10B981),
        background = Color(0xFF09090B),
        surface = Color(0xFF18181B),
        border = Color(0xFF27272A),
        textPrimary = Color(0xFFF4F4F5),
        textMuted = Color(0xFFA1A1AA),
        accent = Color(0xFF10B981),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFF063327),
        cardGradEnd = Color(0xFF18181B)
    )
    val EmeraldLight = ShadcnColors(
        primary = Color(0xFF059669),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF4F4F5),
        border = Color(0xFFE4E4E7),
        textPrimary = Color(0xFF09090B),
        textMuted = Color(0xFF71717A),
        accent = Color(0xFF059669),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFFF0FDF4),
        cardGradEnd = Color(0xFFF4F4F5)
    )

    // 4. ORANGE (Sunset Motivation)
    val OrangeDark = ShadcnColors(
        primary = Color(0xFFF97316),
        background = Color(0xFF09090B),
        surface = Color(0xFF18181B),
        border = Color(0xFF27272A),
        textPrimary = Color(0xFFF4F4F5),
        textMuted = Color(0xFFA1A1AA),
        accent = Color(0xFFF97316),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFF331705),
        cardGradEnd = Color(0xFF18181B)
    )
    val OrangeLight = ShadcnColors(
        primary = Color(0xFFEA580C),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF4F4F5),
        border = Color(0xFFE4E4E7),
        textPrimary = Color(0xFF09090B),
        textMuted = Color(0xFF71717A),
        accent = Color(0xFFEA580C),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFFFFF7ED),
        cardGradEnd = Color(0xFFF4F4F5)
    )
}

// Premium System Fonts Typography
val AppTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun WalkVerseTheme(
    themeName: String = "ZINC",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val shadcnColors = when (themeName) {
        "ROSE" -> if (darkTheme) WalkVerseThemePalettes.RoseDark else WalkVerseThemePalettes.RoseLight
        "EMERALD" -> if (darkTheme) WalkVerseThemePalettes.EmeraldDark else WalkVerseThemePalettes.EmeraldLight
        "ORANGE" -> if (darkTheme) WalkVerseThemePalettes.OrangeDark else WalkVerseThemePalettes.OrangeLight
        else -> if (darkTheme) WalkVerseThemePalettes.ZincDark else WalkVerseThemePalettes.ZincLight // ZINC
    }

    val materialColorScheme = if (darkTheme) {
        darkColorScheme(
            primary = shadcnColors.primary,
            background = shadcnColors.background,
            surface = shadcnColors.surface,
            onPrimary = Color.Black,
            onBackground = shadcnColors.textPrimary,
            onSurface = shadcnColors.textPrimary,
            outline = shadcnColors.border
        )
    } else {
        lightColorScheme(
            primary = shadcnColors.primary,
            background = shadcnColors.background,
            surface = shadcnColors.surface,
            onPrimary = Color.White,
            onBackground = shadcnColors.textPrimary,
            onSurface = shadcnColors.textPrimary,
            outline = shadcnColors.border
        )
    }

    CompositionLocalProvider(
        LocalShadcnColors provides shadcnColors
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
