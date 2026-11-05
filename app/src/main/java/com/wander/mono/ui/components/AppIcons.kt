package com.wander.mono.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.settings.IconMode
import com.wander.mono.data.settings.IconShape
import com.wander.mono.ui.theme.LocalIconMask
import com.wander.mono.ui.theme.toMask

/**
 * What rows need to draw app icons: the user's [mode], and a cache-backed loader for it. [pack]
 * only takes part in equality, so a newly chosen icon pack makes every icon load again.
 */
@Immutable
data class AppIcons(
    val mode: IconMode,
    val load: suspend (key: AppKey, mode: IconMode, sizePx: Int) -> AppIconImage?,
    val pack: String = "",
)

/**
 * An app icon from the in-memory cache, or an empty slot of the same size when [key] is null so
 * rows stay aligned. Themed glyphs are tinted as the user chose ([themedGlyphColour]). Unmasked adaptive icons are cut
 * to the user's shape ([LocalIconMask]); a new shape loads every icon again, since whether one
 * comes unmasked depends on it. Text-only mode draws nothing at all, so labels start at the row's
 * gutter. A glyph standing in for an icon the pack lacks sits on a translucent frame in the user's
 * shape (a squircle with the system's). Decorative: the label next to it names the app.
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
    val icon by produceState<AppIconImage?>(null, key, icons, sizePx, mask) {
        value = key?.let { icons.load(it, icons.mode, sizePx) }
    }
    val image = icon
    if (image != null && image.isFramed) {
        FramedGlyph(image, size, mask ?: IconShape.Squircle.toMask(), modifier)
    } else if (image != null) {
        val tint = if (image.isMonochrome) ColorFilter.tint(themedGlyphColour(image.accent)) else null
        val shaped = if (image.isFullBleed && mask != null) Modifier.clip(mask) else Modifier
        Image(image.bitmap, contentDescription = null, modifier = modifier.size(size).then(shaped), colorFilter = tint)
    } else {
        Box(modifier.size(size))
    }
}

/** [image]'s glyph on a translucent [frame], letting the wallpaper through like a pack's own frames. */
@Composable
private fun FramedGlyph(image: AppIconImage, size: Dp, frame: Shape?, modifier: Modifier) {
    val plate = MaterialTheme.colorScheme.onSurface.copy(alpha = FRAME_ALPHA)
    Box(modifier.size(size).then(if (frame != null) Modifier.background(plate, frame) else Modifier)) {
        Image(
            image.bitmap,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            colorFilter = ColorFilter.tint(themedGlyphColour(image.accent)),
        )
    }
}

/** How much of the text colour the frame takes: a faint plate, mostly wallpaper. */
private const val FRAME_ALPHA = 0.14f
