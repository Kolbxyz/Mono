package com.wander.mono.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.core.apps.AppKey

/** An app icon from the in-memory cache. Decorative: the label next to it names the app. */
@Composable
fun AppIcon(
    key: AppKey,
    loadIcon: suspend (AppKey, Int) -> ImageBitmap?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val icon by produceState<ImageBitmap?>(null, key, sizePx) { value = loadIcon(key, sizePx) }
    val bitmap = icon
    if (bitmap != null) {
        Image(bitmap, contentDescription = null, modifier = modifier.size(size))
    } else {
        Box(modifier.size(size))
    }
}
