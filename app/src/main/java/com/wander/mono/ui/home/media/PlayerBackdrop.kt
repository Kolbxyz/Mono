package com.wander.mono.ui.home.media

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.wander.mono.core.media.PlayingMedia

/**
 * The expanded player's background: its artwork blurred (a render effect, Android 12+) under a
 * [scrim] of the card's tint, so the text keeps its contrast. It fades in with the morph ([shown])
 * and crossfades on a track change. Without artwork, or before Android 12 where a blur cannot be
 * drawn, the card keeps its plain artwork tint.
 */
@Composable
internal fun PlayerBackdrop(media: PlayingMedia, scrim: Color, shown: () -> Float, modifier: Modifier = Modifier) {
    if (media.art == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val effects = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    AnimatedContent(
        targetState = TrackArt.of(media),
        contentKey = { it.key },
        transitionSpec = { fadeIn(effects) togetherWith fadeOut(effects) },
        modifier = modifier.graphicsLayer { alpha = shown().coerceIn(0f, 1f) },
        label = "player-backdrop",
    ) { track ->
        Box(Modifier.fillMaxSize()) {
            track.art?.let { BlurredArt(it) }
            Box(Modifier.fillMaxSize().background(scrim.copy(alpha = SCRIM_ALPHA)))
        }
    }
}

@Composable
private fun BlurredArt(art: ImageBitmap) {
    Image(
        art,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize().blur(BLUR, BlurredEdgeTreatment.Rectangle),
    )
}

/** Strong enough that the artwork reads as colour and light, never as a second picture. */
private val BLUR = 32.dp

/** How much of the tint lies over the blurred artwork. */
private const val SCRIM_ALPHA = 0.7f
