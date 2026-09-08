package com.nullify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

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
    tertiary = NullifyTertiary,
    onTertiary = NullifyOnTertiary,
    tertiaryContainer = NullifyTertiaryContainer,
    onTertiaryContainer = NullifyOnTertiaryContainer,
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
    outline = NullifyOutline,
    outlineVariant = NullifyOutlineVariantLight,
    scrim = NullifyScrim,
    inverseSurface = NullifyInverseSurfaceLight,
    inverseOnSurface = NullifyInverseOnSurfaceLight,
    inversePrimary = NullifyInversePrimaryLight,
    surfaceDim = NullifySurfaceDimLight,
    surfaceBright = NullifySurfaceBrightLight,
    surfaceContainerLowest = NullifySurfaceContainerLowestLight,
    surfaceContainerLow = NullifySurfaceContainerLowLight,
    surfaceContainer = NullifySurfaceContainerLight,
    surfaceContainerHigh = NullifySurfaceContainerHighLight,
    surfaceContainerHighest = NullifySurfaceContainerHighestLight,
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
    tertiary = NullifyTertiaryContainer,
    onTertiary = NullifyOnTertiaryContainer,
    tertiaryContainer = NullifyTertiary,
    onTertiaryContainer = NullifyOnTertiary,
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
    outline = NullifyOutline,
    outlineVariant = NullifyOutlineVariantDark,
    scrim = NullifyScrim,
    inverseSurface = NullifyInverseSurfaceDark,
    inverseOnSurface = NullifyInverseOnSurfaceDark,
    inversePrimary = NullifyInversePrimaryDark,
    surfaceDim = NullifySurfaceDimDark,
    surfaceBright = NullifySurfaceBrightDark,
    surfaceContainerLowest = NullifySurfaceContainerLowestDark,
    surfaceContainerLow = NullifySurfaceContainerLowDark,
    surfaceContainer = NullifySurfaceContainerDark,
    surfaceContainerHigh = NullifySurfaceContainerHighDark,
    surfaceContainerHighest = NullifySurfaceContainerHighestDark,
)

@Composable
expect fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme?

@Composable
expect fun PlatformSystemBarsEffect(darkTheme: Boolean)

val LocalThemeIsDark = staticCompositionLocalOf { false }

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

    PlatformSystemBarsEffect(darkTheme = darkTheme)

    CompositionLocalProvider(LocalThemeIsDark provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NullifyTypography,
            content = content
        )
    }
}

fun ThemeMode.next(): ThemeMode = when (this) {
    ThemeMode.System -> ThemeMode.Light
    ThemeMode.Light -> ThemeMode.Dark
    ThemeMode.Dark -> ThemeMode.System
}
