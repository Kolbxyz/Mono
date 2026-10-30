package com.wander.mono.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * The shadow home text drawn straight on the wallpaper gets, null when the scrim does that job. Not
 * static: the halo fades with the window's focus, and only the text reading it should recompose.
 */
val LocalWallpaperTextShadow = compositionLocalOf<Shadow?> { null }

/**
 * This style with the wallpaper text shadow, when one is on. Only for text drawn on the wallpaper,
 * never inside cards.
 */
@Composable
@ReadOnlyComposable
fun TextStyle.onWallpaper(): TextStyle = LocalWallpaperTextShadow.current?.let { copy(shadow = it) } ?: this

/**
 * The shadow under home's text: the user's text shadow, which replaces the scrim, or else a soft
 * halo in the scrim's colour that carries the text where the eased scrim has thinned out, at [halo]
 * of its strength (it fades out with the scrim while the window is not focused).
 */
@Composable
@ReadOnlyComposable
internal fun wallpaperTextShadow(textShadow: Boolean, halo: Float): Shadow {
    val scheme = MaterialTheme.colorScheme
    return with(LocalDensity.current) {
        if (textShadow) {
            Shadow(scheme.scrim.copy(alpha = SHADOW_ALPHA), Offset(0f, 1.dp.toPx()), blurRadius = 4.dp.toPx())
        } else {
            Shadow(scheme.surface.copy(alpha = HALO_ALPHA * halo), Offset.Zero, blurRadius = 12.dp.toPx())
        }
    }
}

/** Text shadow strength, per spec 5.4: the `scrim` role at 40 %. */
private const val SHADOW_ALPHA = 0.4f

/** The halo under home text when the scrim is on: the surface colour, soft and wide. */
private const val HALO_ALPHA = 0.55f
