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

// Custom Theme Palettes
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
        background = Color(0xFF0C0A09),
        surface = Color(0xFF1C1917),
        border = Color(0xFF2D2A27),
        textPrimary = Color(0xFFF5F5F4),
        textMuted = Color(0xFFA8A29E),
        accent = Color(0xFFF43F5E),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFF2E1018),
        cardGradEnd = Color(0xFF1C1917)
    )
    val RoseLight = ShadcnColors(
        primary = Color(0xFFE11D48),
        background = Color(0xFFFFF1F2),
        surface = Color(0xFFFFE4E6),
        border = Color(0xFFFDA4AF),
        textPrimary = Color(0xFF4C0519),
        textMuted = Color(0xFF9F1239),
        accent = Color(0xFFE11D48),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFFFFE4E6),
        cardGradEnd = Color(0xFFFECDD3)
    )

    // 3. EMERALD (Nature & Growth)
    val EmeraldDark = ShadcnColors(
        primary = Color(0xFF10B981),
        background = Color(0xFF022C22),
        surface = Color(0xFF064E3B),
        border = Color(0xFF047857),
        textPrimary = Color(0xFFECFDF5),
        textMuted = Color(0xFFA7F3D0),
        accent = Color(0xFF34D399),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFF064E3B),
        cardGradEnd = Color(0xFF022C22)
    )
    val EmeraldLight = ShadcnColors(
        primary = Color(0xFF059669),
        background = Color(0xFFF0FDF4),
        surface = Color(0xFFDCFCE7),
        border = Color(0xFFA7F3D0),
        textPrimary = Color(0xFF064E3B),
        textMuted = Color(0xFF047857),
        accent = Color(0xFF059669),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFFDCFCE7),
        cardGradEnd = Color(0xFFBBF7D0)
    )

    // 4. ORANGE (Sunset Motivation)
    val OrangeDark = ShadcnColors(
        primary = Color(0xFFF97316),
        background = Color(0xFF0C0A09),
        surface = Color(0xFF1C1917),
        border = Color(0xFF2D2A27),
        textPrimary = Color(0xFFF5F5F4),
        textMuted = Color(0xFFA8A29E),
        accent = Color(0xFFF97316),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFF2C1609),
        cardGradEnd = Color(0xFF1C1917)
    )
    val OrangeLight = ShadcnColors(
        primary = Color(0xFFEA580C),
        background = Color(0xFFFFF7ED),
        surface = Color(0xFFFFEDD5),
        border = Color(0xFFFED7AA),
        textPrimary = Color(0xFF431407),
        textMuted = Color(0xFF9A3412),
        accent = Color(0xFFEA580C),
        success = Color(0xFF10B981),
        warning = Color(0xFFF59E0B),
        cardGradStart = Color(0xFFFFEDD5),
        cardGradEnd = Color(0xFFFFDBB5)
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
