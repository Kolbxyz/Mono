package com.wander.mono.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.theme.LocalIconMask

/**
 * What rows need to draw app icons: the user's [mode], a cache-backed loader for it, and [peek],
 * which gives an icon already in memory at once so a row shows it in its very first frame. [pack]
 * only takes part in equality, so a newly chosen icon pack makes every icon load again. Pass
 * function references for [load] and [peek], so equal icons compare equal.
 */
@Immutable
data class AppIcons(
    val mode: IconMode,
    val load: suspend (key: AppKey, mode: IconMode, sizePx: Int) -> AppIconImage?,
    val peek: (key: AppKey, mode: IconMode, sizePx: Int) -> AppIconImage?,
    val pack: String = "",
)

/**
 * An app icon from the in-memory cache (there from the first frame when it is cached already), or an
 * empty slot of the same size when [key] is null so rows stay aligned. Themed glyphs are tinted as
 * the user chose ([themedGlyphColour]). Unmasked adaptive icons are cut to the user's shape
 * ([LocalIconMask]); a new shape loads every icon again, since whether one comes unmasked depends
 * on it. Text-only mode draws nothing at all, so labels start at the row's gutter. Decorative: the
 * label next to it names the app.
 */
@Composable
fun AppIcon(
    key: AppKey?,
    icons: AppIcons,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    if (icons.mode == IconMode.TextOnly) return
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val mask = LocalIconMask.current
    // In memory already, it draws in the first frame instead of blinking in a frame later.
    val icon by produceState(key?.let { icons.peek(it, icons.mode, sizePx) }, key, icons, sizePx, mask) {
        value = key?.let { icons.peek(it, icons.mode, sizePx) ?: icons.load(it, icons.mode, sizePx) }
    }
    val image = icon
    if (image != null) {
        val tint = if (image.isMonochrome) ColorFilter.tint(themedGlyphColour(image.accent)) else null
        val shaped = if (image.isFullBleed && mask != null) Modifier.clip(mask) else Modifier
        Image(image.bitmap, contentDescription = null, modifier = modifier.size(size).then(shaped), colorFilter = tint)
    } else {
        Box(modifier.size(size))
    }
}
