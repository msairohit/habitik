package com.habitik.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


// ── Theme Management ────────────────────────────────────────────────────────────

enum class AppTheme(val primary: Color, val secondary: Color) {
    Purple(ThemePurple, GlowCyan),
    Blue(ThemeBlue, BrandMint),
    Emerald(ThemeEmerald, BrandYellow),
    Rose(ThemeRose, BrandBlue),
    Amber(ThemeAmber, BrandPink)
}

object ThemeManager {
    var currentTheme = androidx.compose.runtime.mutableStateOf(AppTheme.Purple)
}

// ── Color Schemes ──────────────────────────────────────────────────────────────

private fun getDarkColorScheme(primaryColor: Color, secondaryColor: Color) = darkColorScheme(
    primary              = primaryColor,
    onPrimary            = Color.White,
    primaryContainer     = primaryColor.copy(alpha = 0.2f),
    onPrimaryContainer   = Color(0xFFE3F2FD),
    secondary            = secondaryColor,
    onSecondary          = Color.White,
    secondaryContainer   = secondaryColor.copy(alpha = 0.1f),
    onSecondaryContainer = Color(0xFFEDE7F6),
    tertiary             = TertiaryDark,
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFF00695C),
    onTertiaryContainer  = Color(0xFFB2DFDB),
    background           = BgBase,
    onBackground         = Color(0xFFE3F2FD),
    surface              = BgSurface,
    onSurface            = Color(0xFFE3F2FD),
    surfaceVariant       = BgCard,
    onSurfaceVariant     = Color(0xFFB0BEC5),
    outline              = primaryColor.copy(alpha = 0.5f),
    outlineVariant       = Color(0xFF37474F),
    error                = Color(0xFFFF5252),
    onError              = Color.White,
    errorContainer       = Color(0xFFB71C1C),
    onErrorContainer     = Color(0xFFFFCDD2),
    inverseSurface       = Color(0xFFE3F2FD),
    inverseOnSurface     = BgBase,
    inversePrimary       = BrandBlue,
    scrim                = Color(0xFF000000)
)

private fun getLightColorScheme(primaryColor: Color, secondaryColor: Color) = lightColorScheme(
    primary              = primaryColor,
    onPrimary            = Color.White,
    primaryContainer     = primaryColor.copy(alpha = 0.1f),
    onPrimaryContainer   = primaryColor,
    secondary            = secondaryColor,
    onSecondary          = Color.White,
    secondaryContainer   = secondaryColor.copy(alpha = 0.05f),
    onSecondaryContainer = secondaryColor,
    tertiary             = TertiaryLight,
    onTertiary           = Color.White,
    background           = BackgroundLight,
    onBackground         = Color(0xFF1A1C1E),
    surface              = SurfaceLight,
    onSurface            = Color(0xFF1A1C1E),
    surfaceVariant       = CardAltLight,
    onSurfaceVariant     = Color(0xFF44474E),
    outline              = primaryColor.copy(alpha = 0.3f),
    outlineVariant       = Color(0xFFC4C6CF)
)

@Composable
fun HabitikTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    theme: AppTheme = ThemeManager.currentTheme.value,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        getDarkColorScheme(theme.primary, theme.secondary)
    } else {
        getLightColorScheme(theme.primary, theme.secondary)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography   = Typography,
        content      = content
    )
}

