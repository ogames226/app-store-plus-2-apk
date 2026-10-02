package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.local.ThemeMode

fun buildDarkColorScheme(primaryColor: Color, cardBg: Color = SurfaceDark): ColorScheme {
    return darkColorScheme(
        primary = primaryColor,
        onPrimary = Color.White,
        primaryContainer = primaryColor.copy(alpha = 0.8f),
        onPrimaryContainer = Color.White,
        secondary = BrandPurple,
        onSecondary = Color.White,
        secondaryContainer = SurfaceVariantDark,
        onSecondaryContainer = Color.White,
        tertiary = BrandPink,
        background = DarkBg,
        onBackground = TextPrimary,
        surface = cardBg,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = TextSecondary,
        outline = BorderSubtle
    )
}

fun buildLightColorScheme(primaryColor: Color): ColorScheme {
    return lightColorScheme(
        primary = primaryColor,
        onPrimary = Color.White,
        primaryContainer = primaryColor.copy(alpha = 0.15f),
        onPrimaryContainer = primaryColor,
        secondary = BrandPurple,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF1F5F9),
        onSecondaryContainer = Color(0xFF1E293B),
        tertiary = BrandPink,
        background = Color(0xFFF8FAFC),
        onBackground = Color(0xFF0F172A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFF1F5F9),
        onSurfaceVariant = Color(0xFF64748B),
        outline = Color(0xFFE2E8F0)
    )
}

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    customPrimaryColor: Color = BrandBlue,
    cardStyle: String = "DARK_SLATE",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemDark
    }

    val cardColor = when (cardStyle) {
        "DEEP_BLACK" -> Color(0xFF0A0E17)
        "MIDNIGHT_NAVY" -> Color(0xFF0F172A)
        else -> SurfaceDark
    }

    val colorScheme = if (isDark) {
        buildDarkColorScheme(customPrimaryColor, cardColor)
    } else {
        buildLightColorScheme(customPrimaryColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
