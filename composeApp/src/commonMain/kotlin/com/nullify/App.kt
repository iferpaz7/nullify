package com.nullify

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.nullify.ui.NullifyViewModel
import com.nullify.ui.WhitelistScreen
import com.nullify.ui.theme.GlassGradientEndDark
import com.nullify.ui.theme.GlassGradientEndLight
import com.nullify.ui.theme.GlassGradientMidDark
import com.nullify.ui.theme.GlassGradientMidLight
import com.nullify.ui.theme.GlassGradientStartDark
import com.nullify.ui.theme.GlassGradientStartLight
import com.nullify.ui.theme.NullifyTheme
import com.nullify.ui.theme.ThemeMode
import com.nullify.ui.theme.next

@Composable
fun NullifyApp(viewModel: NullifyViewModel) {
    var themeMode by remember { mutableStateOf(ThemeMode.System) }

    NullifyTheme(themeMode = themeMode) {
        val isDark = when (themeMode) {
            ThemeMode.System -> isSystemInDarkTheme()
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }

        val gradientColors = if (isDark) {
            listOf(
                GlassGradientStartDark,
                GlassGradientMidDark,
                GlassGradientEndDark,
            )
        } else {
            listOf(
                GlassGradientStartLight,
                GlassGradientMidLight,
                GlassGradientEndLight,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = gradientColors,
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY,
                    )
                )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent,
            ) {
                WhitelistScreen(
                    viewModel = viewModel,
                    themeMode = themeMode,
                    onCycleTheme = { themeMode = themeMode.next() },
                )
            }
        }
    }
}
