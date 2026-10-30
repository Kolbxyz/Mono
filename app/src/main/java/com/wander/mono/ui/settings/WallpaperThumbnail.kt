package com.wander.mono.ui.settings

import android.content.res.Resources
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A bundled wallpaper filling its box, fading in once decoded, under [scrim] when given. Until
 * then (or if it cannot be decoded) the box keeps whatever its container draws.
 */
@Composable
internal fun WallpaperThumbnail(
    @DrawableRes image: Int,
    width: Dp,
    modifier: Modifier = Modifier,
    scrim: Color? = null,
) {
    val thumbnail = rememberWallpaperThumbnail(image, width)
    AnimatedVisibility(
        visible = thumbnail != null,
        enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
        modifier = modifier,
    ) {
        Box(Modifier.fillMaxSize()) {
            thumbnail?.let { Image(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            scrim?.let { Box(Modifier.fillMaxSize().background(it)) }
        }
    }
}

/**
 * [image] scaled down for a preview at least [width] wide, decoded off the main thread at a
 * power-of-two sample size, so a 1440 px image never sits in memory whole. Null while it decodes,
 * or if it cannot be decoded.
 */
@Composable
private fun rememberWallpaperThumbnail(@DrawableRes image: Int, width: Dp): ImageBitmap? {
    val resources = LocalResources.current
    val widthPx = with(LocalDensity.current) { width.roundToPx() }
    val thumbnail by produceState<ImageBitmap?>(null, image, widthPx) {
        value = withContext(Dispatchers.IO) { decodeThumbnail(resources, image, widthPx) }
    }
    return thumbnail
}

private fun decodeThumbnail(resources: Resources, @DrawableRes image: Int, widthPx: Int): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeResource(resources, image, bounds)
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize(bounds.outWidth, widthPx) }
    return BitmapFactory.decodeResource(resources, image, options)?.asImageBitmap()
}

/** The largest power of two that still leaves [width] at least [target] wide. */
internal fun sampleSize(width: Int, target: Int): Int {
    var sample = 1
    while (target > 0 && width / (sample * 2) >= target) sample *= 2
    return sample
}
