package com.wander.mono.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.IconMode

/** What rows need to draw app icons: the user's [mode], and a cache-backed loader for it. */
@Immutable
data class AppIcons(
    val mode: IconMode,
    val load: suspend (key: AppKey, mode: IconMode, sizePx: Int) -> AppIconImage?,
)

/**
 * An app icon from the in-memory cache, or an empty slot of the same size when [key] is null so
 * rows stay aligned. Themed glyphs are tinted `onSurfaceVariant`. Text-only mode draws nothing at
 * all, so labels start at the row's gutter. Decorative: the label next to it names the app.
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
    val icon by produceState<AppIconImage?>(null, key, icons, sizePx) {
        value = key?.let { icons.load(it, icons.mode, sizePx) }
    }
    val image = icon
    if (image != null) {
        val tint = if (image.isMonochrome) ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant) else null
        Image(image.bitmap, contentDescription = null, modifier = modifier.size(size), colorFilter = tint)
    } else {
        Box(modifier.size(size))
    }
}
