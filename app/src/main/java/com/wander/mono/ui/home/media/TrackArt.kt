package com.wander.mono.ui.home.media

import androidx.compose.ui.graphics.ImageBitmap
import com.wander.mono.core.media.PlayingMedia

/**
 * The artwork of one track. Sessions republish their artwork with every state change, so what
 * crossfades is keyed by the track ([key]), not by the bitmap: a new track fades in, a pause does not.
 */
internal data class TrackArt(val key: Pair<String, String?>, val art: ImageBitmap?) {
    companion object {
        fun of(media: PlayingMedia) = TrackArt(media.title to media.artist, media.art)
    }
}
