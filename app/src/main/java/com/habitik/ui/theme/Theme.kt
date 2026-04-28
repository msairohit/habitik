package com.habitik.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Signature Brand Colors ─────────────────────────────────────────────────────
val BrandViolet      = Color(0xFF7C3AED) // Violet 600  – primary
val BrandIndigo      = Color(0xFF4F46E5) // Indigo 600
val BrandCyan        = Color(0xFF06B6D4) // Cyan 500     – accent
val BrandRose        = Color(0xFFF43F5E) // Rose 500     – tertiary

// Light palette
val PrimaryLight     = BrandViolet
val SecondaryLight   = BrandIndigo
val TertiaryLight    = BrandRose
val PrimaryDark      = Color(0xFFA78BFA) // Violet 400
val SecondaryDark    = Color(0xFF818CF8) // Indigo 400
val TertiaryDark     = Color(0xFFFB7185) // Rose 400

// ── Background Layers (Dark) ───────────────────────────────────────────────────
// True deep "amoled-ish" navy – not pure black so gradients pop nicely
val BgBase           = Color(0xFF07090F) // ~Slate 960
val BgSurface        = Color(0xFF0D1117) // Slate 930
val BgCard           = Color(0xFF161B26) // Slate 900 lightened
val BgCardAlt        = Color(0xFF1C2333) // Slate 800

// Top-of-screen decorative tint colours (used in radial gradient overlay)
val GlowViolet       = Color(0xFF7C3AED)
val GlowCyan         = Color(0xFF06B6D4)

// Light backgrounds
val BackgroundLight  = Color(0xFFF0F4FF)
val SurfaceLight     = Color(0xFFFFFFFF)

// ── Color Schemes ──────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = PrimaryDark,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFF3B1F8C),
    onPrimaryContainer   = Color(0xFFE9D8FF),
    secondary            = SecondaryDark,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFF252E6B),
    onSecondaryContainer = Color(0xFFD9DEFF),
    tertiary             = TertiaryDark,
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFF6B1830),
    onTertiaryContainer  = Color(0xFFFFD9DF),
    background           = BgBase,
    onBackground         = Color(0xFFEAEEF9),
    surface              = BgSurface,
    onSurface            = Color(0xFFEAEEF9),
    surfaceVariant       = BgCard,
    onSurfaceVariant     = Color(0xFF8899BF),
    outline              = Color(0xFF2D3650),
    outlineVariant       = Color(0xFF1E2840),
    error                = Color(0xFFFF6B6B),
    onError              = Color.White,
    errorContainer       = Color(0xFF4A1010),
    onErrorContainer     = Color(0xFFFFCDD5),
    inverseSurface       = Color(0xFFEAEEF9),
    inverseOnSurface     = BgBase,
    inversePrimary       = BrandViolet,
    scrim                = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary              = PrimaryLight,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFEDE9FE),
    onPrimaryContainer   = Color(0xFF3B1F8C),
    secondary            = SecondaryLight,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF252E6B),
    tertiary             = TertiaryLight,
    onTertiary           = Color.White,
    background           = BackgroundLight,
    onBackground         = Color(0xFF0F172A),
    surface              = SurfaceLight,
    onSurface            = Color(0xFF0F172A),
    surfaceVariant       = Color(0xFFEDE9FE),
    onSurfaceVariant     = Color(0xFF64748B),
    outline              = Color(0xFFCBD5E1)
)

@Composable
fun HabitikTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography   = Typography,
        content      = content
    )
}
