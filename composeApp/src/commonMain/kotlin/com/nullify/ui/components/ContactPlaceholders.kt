package com.nullify.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nullify.ui.theme.GlassBorderDark
import com.nullify.ui.theme.GlassBorderLight
import com.nullify.ui.theme.LocalThemeIsDark

/**
 * Shimmer gradient brush calibrated for Dark and Light theme strategies.
 * In Dark mode: soft white highlights with low alpha to prevent high-luminance glare.
 * In Light mode: crisp on-surface tones that blend gently with glassmorphism backgrounds.
 */
@Composable
fun rememberShimmerBrush(
    isDark: Boolean = LocalThemeIsDark.current,
): Brush {
    val shimmerColors = if (isDark) {
        listOf(
            Color(0xFFFFFFFF).copy(alpha = 0.04f),
            Color(0xFFFFFFFF).copy(alpha = 0.15f),
            Color(0xFFFFFFFF).copy(alpha = 0.04f),
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        )
    }

    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1300,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ShimmerTranslate",
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim - 350f, y = translateAnim - 350f),
        end = Offset(x = translateAnim, y = translateAnim),
    )
}

/**
 * Skeleton card representing a contact item while the list is loading.
 */
@Composable
fun ContactSkeletonCard(
    modifier: Modifier = Modifier,
    shimmerBrush: Brush = rememberShimmerBrush(),
) {
    val isDark = LocalThemeIsDark.current
    val shape = RoundedCornerShape(16.dp)
    val glassBorder = if (isDark) GlassBorderDark else GlassBorderLight
    val glassAlpha = if (isDark) 0.55f else 0.70f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = glassAlpha),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar skeleton
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(shimmerBrush)
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Name and number placeholders
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.38f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Action icon placeholder
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(shimmerBrush)
                )
            }

            // Glass border
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .border(0.5.dp, glassBorder, shape)
            )
        }
    }
}

/**
 * Full skeleton loading list for WhitelistScreen.
 */
@Composable
fun ContactListSkeleton(
    modifier: Modifier = Modifier,
    count: Int = 4,
) {
    val shimmerBrush = rememberShimmerBrush()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(count) {
            ContactSkeletonCard(shimmerBrush = shimmerBrush)
        }
    }
}

/**
 * Contact avatar placeholder for each contact card in the list.
 * Displays initials if the contact name has letters, or an icon fallback.
 * Uses adaptive tonal styling for Light and Dark modes.
 */
@Composable
fun ContactAvatarPlaceholder(
    displayName: String,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val initials = extractInitials(displayName)

    val containerColor = if (isDark) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
    }

    val contentColor = if (isDark) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val borderColor = if (isDark) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    } else {
        GlassBorderLight
    }

    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(containerColor)
            .border(0.8.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (initials.isNotEmpty()) {
            Text(
                text = initials,
                color = contentColor,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                ),
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/**
 * Rich empty-state placeholder tailored for WhitelistScreen in both Light and Dark modes.
 * Dynamically switches between 'empty whitelist' and 'no search results' modes.
 */
@Composable
fun ContactEmptyStatePlaceholder(
    searchQuery: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val isSearching = searchQuery.isNotBlank()
    val shape = RoundedCornerShape(20.dp)
    val glassBorder = if (isDark) GlassBorderDark else GlassBorderLight
    val cardAlpha = if (isDark) 0.50f else 0.75f

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Badge container for icon
                    val iconBg = if (isDark) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)
                    }
                    val iconTint = if (isDark) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(iconBg)
                            .border(
                                1.dp,
                                if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.30f) else GlassBorderLight,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.PersonSearch else Icons.Default.Shield,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isSearching) {
                            "Sin resultados de búsqueda"
                        } else {
                            "Lista de exclusión vacía"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isSearching) {
                            "No se encontraron contactos ni números que coincidan con \"$searchQuery\"."
                        } else {
                            "Ningún número agregado manualmente aún.\nLos contactos de tu libreta se sincronizan automáticamente."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                    )

                    if (isSearching) {
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedButton(
                            onClick = onClearSearch,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Limpiar búsqueda")
                        }
                    }
                }

                // Glass border
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(shape)
                        .border(0.5.dp, glassBorder, shape)
                )
            }
        }
    }
}

/**
 * Extracts 1 or 2 uppercase initials from a display name.
 */
private fun extractInitials(name: String): String {
    val clean = name.trim()
    if (clean.isEmpty()) return ""
    val words = clean.split(Regex("\\s+")).filter { it.isNotEmpty() }
    val chars = words.mapNotNull { word ->
        word.firstOrNull { it.isLetter() }
    }
    return when {
        chars.size >= 2 -> "${chars[0]}${chars[1]}".uppercase()
        chars.size == 1 -> "${chars[0]}".uppercase()
        else -> ""
    }
}
