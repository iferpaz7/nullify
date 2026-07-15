package com.nullify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class ThemeMode { System, Light, Dark }

private val LightColorScheme = lightColorScheme(
    primary = NullifyPrimary,
    onPrimary = NullifyOnPrimary,
    primaryContainer = NullifyPrimaryContainer,
    onPrimaryContainer = NullifyOnPrimaryContainer,
    secondary = NullifySecondary,
    onSecondary = NullifyOnSecondary,
    secondaryContainer = NullifySecondaryContainer,
    onSecondaryContainer = NullifyOnSecondaryContainer,
    error = NullifyError,
    onError = NullifyOnError,
    errorContainer = NullifyErrorContainer,
    onErrorContainer = NullifyOnErrorContainer,
    background = NullifyBackground,
    onBackground = NullifyOnBackground,
    surface = NullifySurface,
    onSurface = NullifyOnSurface,
    surfaceVariant = NullifySurfaceVariant,
    onSurfaceVariant = NullifyOnSurfaceVariant,
    outline = NullifyOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = NullifyPrimaryContainer,
    onPrimary = NullifyOnPrimaryContainer,
    primaryContainer = NullifyPrimary,
    onPrimaryContainer = NullifyOnPrimary,
    secondary = NullifySecondaryContainer,
    onSecondary = NullifyOnSecondaryContainer,
    secondaryContainer = NullifySecondary,
    onSecondaryContainer = NullifyOnSecondary,
    error = NullifyErrorContainer,
    onError = NullifyOnErrorContainer,
    errorContainer = NullifyError,
    onErrorContainer = NullifyOnError,
    background = NullifyOnBackground,
    onBackground = NullifyBackground,
    surface = NullifyOnSurface,
    onSurface = NullifyBackground,
    surfaceVariant = NullifyOnSurfaceVariant,
    onSurfaceVariant = NullifySurfaceVariant,
    outline = NullifyOutline
)

@Composable
expect fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme?

@Composable
fun NullifyTheme(
    themeMode: ThemeMode = ThemeMode.System,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val colorScheme = if (dynamicColor) {
        getDynamicColorScheme(darkTheme) ?: if (darkTheme) DarkColorScheme else LightColorScheme
    } else {
        if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NullifyTypography,
        content = content
    )
}

fun ThemeMode.next(): ThemeMode = when (this) {
    ThemeMode.System -> ThemeMode.Light
    ThemeMode.Light -> ThemeMode.Dark
    ThemeMode.Dark -> ThemeMode.System
}
