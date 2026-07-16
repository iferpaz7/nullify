package com.nullify

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nullify.ui.CallLogScreen
import com.nullify.ui.NullifyViewModel
import com.nullify.ui.WhitelistScreen
import com.nullify.ui.theme.GlassBorderDark
import com.nullify.ui.theme.GlassBorderLight
import com.nullify.ui.theme.GlassGradientEndDark
import com.nullify.ui.theme.GlassGradientEndLight
import com.nullify.ui.theme.GlassGradientMidDark
import com.nullify.ui.theme.GlassGradientMidLight
import com.nullify.ui.theme.GlassGradientStartDark
import com.nullify.ui.theme.GlassGradientStartLight
import com.nullify.ui.theme.NullifyTheme
import com.nullify.ui.theme.ThemeMode
import com.nullify.ui.theme.next

private enum class Tab { Whitelist, CallLog }

@Composable
fun NullifyApp(viewModel: NullifyViewModel) {
    var themeMode by remember { mutableStateOf(ThemeMode.System) }
    var currentTab by remember { mutableStateOf(Tab.Whitelist) }

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

        val glassBorder = if (isDark) GlassBorderDark else GlassBorderLight

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
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.60f),
                            tonalElevation = 0.dp,
                        ) {
                            NavigationBarItem(
                                selected = currentTab == Tab.Whitelist,
                                onClick = { currentTab = Tab.Whitelist },
                                icon = {
                                    Icon(Icons.Default.List, contentDescription = "Lista blanca")
                                },
                                label = { Text("Lista Blanca") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                ),
                            )
                            NavigationBarItem(
                                selected = currentTab == Tab.CallLog,
                                onClick = { currentTab = Tab.CallLog },
                                icon = {
                                    Icon(Icons.Default.History, contentDescription = "Historial")
                                },
                                label = { Text("Historial") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                ),
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        when (currentTab) {
                            Tab.Whitelist -> WhitelistScreen(
                                viewModel = viewModel,
                                themeMode = themeMode,
                                onCycleTheme = { themeMode = themeMode.next() },
                            )
                            Tab.CallLog -> CallLogScreen(
                                viewModel = viewModel,
                                glassBorder = glassBorder,
                            )
                        }
                    }
                }
            }
        }
    }
}
