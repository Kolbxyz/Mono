package com.wander.mono.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.readableAccent
import com.wander.mono.data.settings.ThemedIconTint
import com.wander.mono.ui.theme.LocalIconTheming

/**
 * The app's colour for its row when "Tint rows with app colour" is on, else null (also while the
 * icon loads, and for an app whose icon has no colour of its own). It clears 3:1 against the
 * theme's surface, so it can colour a mark, a progress track or a container's edge as is (text
 * wants 4.5:1: `readableAccent` with that); for a soft container, use it at low alpha.
 *
 * It comes from the same cached icon the row's [AppIcon] draws, so pass the same [icons] and
 * [size] and it costs no decode. Adopters: the favourite and live rows in `ui/home`, the drawer's
 * rows in `ui/drawer`.
 */
@Composable
fun rememberRowAccent(key: AppKey?, icons: AppIcons, size: Dp = 40.dp): Color? {
    val tintRows = LocalIconTheming.current.tintRows
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    // From memory at once when the icon is cached, so a row never shifts colour a frame in.
    val cached = if (tintRows && key != null) icons.peek(key, icons.mode, sizePx)?.accent else null
    val accent by produceState(cached, key, icons, sizePx, tintRows) {
        value = if (tintRows && key != null) {
            (icons.peek(key, icons.mode, sizePx) ?: icons.load(key, icons.mode, sizePx))?.accent
        } else {
            null
        }
    }
    return accent?.let { readableOnSurface(it) }
}

/**
 * The colour a themed glyph is drawn in, by the user's [ThemedIconTint]: the neutral
 * `onSurfaceVariant`, the app's own [accent] (neutral when its icon has no colour), or a scheme
 * role, all of which already read on the surface.
 */
@Composable
@ReadOnlyComposable
internal fun themedGlyphColour(accent: Int?): Color {
    val scheme = MaterialTheme.colorScheme
    return when (LocalIconTheming.current.themedTint) {
        ThemedIconTint.Neutral -> scheme.onSurfaceVariant
        ThemedIconTint.App -> accent?.let { readableOnSurface(it) } ?: scheme.onSurfaceVariant
        ThemedIconTint.Primary -> scheme.primary
        ThemedIconTint.Secondary -> scheme.secondary
        ThemedIconTint.Tertiary -> scheme.tertiary
    }
}

/** [accent] nudged until it clears 3:1 against the theme's surface, WCAG's bar for marks. */
@Composable
@ReadOnlyComposable
private fun readableOnSurface(accent: Int): Color =
    Color(readableAccent(accent, MaterialTheme.colorScheme.surface.toArgb()))
