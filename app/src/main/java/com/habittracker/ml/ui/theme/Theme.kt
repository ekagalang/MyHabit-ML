package com.habittracker.ml.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = TextDark,

    secondary = AccentSuccess,
    onSecondary = TextOnPrimary,

    tertiary = Highlight,
    onTertiary = TextOnPrimary,

    background = BackgroundDark,
    onBackground = TextDark,

    surface = SurfaceDark,
    onSurface = TextDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextDarkMuted,

    error = AccentError,
    onError = TextOnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = TextMain,

    secondary = AccentSuccess,
    onSecondary = TextOnPrimary,

    tertiary = Highlight,
    onTertiary = TextOnPrimary,

    background = BackgroundLight,
    onBackground = TextMain,

    surface = SurfaceLight,
    onSurface = TextMain,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextMuted,

    error = AccentError,
    onError = TextOnPrimary
)

@Composable
fun MyHabitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}