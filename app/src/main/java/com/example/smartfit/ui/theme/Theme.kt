// app/src/main/java/com/example/smartfit/ui/theme/Theme.kt
package com.example.smartfit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ---------------- Light / Dark ColorSchemes ----------------

private val LightColorScheme = lightColorScheme(
    primary        = LimePrimary,
    onPrimary      = Color(0xFF020617),

    secondary      = LightSecondary,
    onSecondary    = Color.White,

    background     = LightBackground,
    onBackground   = LightOnBackground,

    surface        = LightSurface,
    onSurface      = LightOnSurface,

    surfaceVariant = LightSurfaceGlass,
    outline        = LightOutline,

    error          = AccentRed,
    onError        = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary        = LimePrimary,
    onPrimary      = Color.White,

    secondary      = DarkSecondary,
    onSecondary    = Color.Black,

    background     = DarkBackground,
    onBackground   = DarkOnBackground,

    surface        = DarkSurface,
    onSurface      = DarkOnSurface,

    surfaceVariant = DarkCard,
    outline        = DarkOutline,

    error          = AccentRed,
    onError        = Color.Black
)

@Composable
fun SmartFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography  = SmartFitTypography,
        content     = content
    )
}

/**
 * Helper to check if the current SmartFit theme is dark.
 * Use this instead of isSystemInDarkTheme() inside composables.
 */
@Composable
fun isSmartFitDarkTheme(): Boolean {
    val colorScheme = MaterialTheme.colorScheme
    return colorScheme.background == DarkBackground
}

@Composable
fun tipUserBubbleColor(): Color =
    if (isSystemInDarkTheme()) TipBubbleUserDark else TipBubbleUserLight

@Composable
fun tipBotBubbleColor(): Color =
    if (isSystemInDarkTheme()) TipBubbleBotDark else TipBubbleBotLight

@Composable
fun tipInputBarColor(): Color =
    if (isSystemInDarkTheme()) TipInputDark else TipInputLight

@Composable
fun tipBorderColor(): Color =
    if (isSystemInDarkTheme()) TipBorderDark else TipBorderLight
