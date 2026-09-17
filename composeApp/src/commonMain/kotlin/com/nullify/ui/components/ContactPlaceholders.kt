package com.nullify.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Shimmer sweep built ONLY from color-scheme tonal tokens.
 * No hardcoded whites, no per-theme alpha tuning: the sweep is visible yet
 * glare-free in both light and dark schemes, including dynamic color.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val base = MaterialTheme.colorScheme.surfaceContainerHighest
    val highlight = MaterialTheme.colorScheme.surfaceContainerLowest

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
        colors = listOf(base, highlight, base),
        start = Offset(x = translateAnim - 350f, y = translateAnim - 350f),
        end = Offset(x = translateAnim, y = translateAnim),
    )
}

/**
 * Skeleton card representing a contact item while the list is loading.
 * Tonal M3: skeleton shapes over a surfaceContainerLow card.
 */
@Composable
fun ContactSkeletonCard(
    modifier: Modifier = Modifier,
    shimmerBrush: Brush = rememberShimmerBrush(),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar skeleton
        Box(
            modifier = Modifier
                .size(40.dp)
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
 * Contact avatar following the M3 list pattern: primaryContainer circle
 * with onPrimaryContainer initials (or a Person icon fallback).
 * Works in light, dark and dynamic-color schemes with no manual branches.
 */
@Composable
fun ContactAvatarPlaceholder(
    displayName: String,
    modifier: Modifier = Modifier,
) {
    val initials = extractInitials(displayName)

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (initials.isNotEmpty()) {
            Text(
                text = initials,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/**
 * M3 empty state: centered icon badge on secondaryContainer, headline,
 * supporting text and a tonal action button when searching.
 */
@Composable
fun ContactEmptyStatePlaceholder(
    searchQuery: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSearching = searchQuery.isNotBlank()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isSearching) Icons.Default.PersonSearch else Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
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
            )

            if (isSearching) {
                Spacer(modifier = Modifier.height(20.dp))
                FilledTonalButton(
                    onClick = onClearSearch,
                    shape = RoundedCornerShape(12.dp),
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
