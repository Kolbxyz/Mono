package com.wander.mono.ui.home.media

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.ui.components.WavyProgressLine
import com.wander.mono.ui.components.WavySeekBar
import com.wander.mono.ui.home.MediaTransport
import com.wander.mono.ui.home.OutputChip
import com.wander.mono.ui.home.PlayPauseButton
import com.wander.mono.ui.home.UpNextList
import com.wander.mono.ui.theme.isLargeFontScale

/**
 * The collapsed card: artwork | title and artist | play/pause, the artwork and the button the same
 * height so the row balances, with a thin wavy progress line beneath. Above 1.5× font scale the
 * button moves under the text so the title keeps its width.
 */
@Composable
internal fun NowPlayingCollapsed(media: PlayingMedia, tint: Color, onTogglePlay: () -> Unit) {
    val stacked = isLargeFontScale()
    Column(Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(INSET),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MediaArt(media.art, COLLAPSED_ART)
            MediaTitle(media, large = false, modifier = Modifier.weight(1f))
            if (!stacked) PlayPauseButton(media.isPlaying, tint, onTogglePlay)
        }
        if (stacked) {
            PlayPauseButton(media.isPlaying, tint, onTogglePlay, Modifier.align(Alignment.End).padding(top = GAP))
        }
        media.progress?.let { progress ->
            val position = rememberPlaybackPosition(progress)
            WavyProgressLine(
                fraction = { position().toFloat() / progress.durationMs },
                isPlaying = progress.isAdvancing,
                color = LocalContentColor.current,
                modifier = Modifier.padding(top = GAP),
            )
        }
    }
}

/**
 * The expanded card, centred on one axis: large artwork, title and artist, the wavy seek bar, the
 * connected transport, then where it is heard and what plays next. The artwork scales in with the
 * card's own spatial spec, so it grows with the card rather than after it.
 */
@Composable
internal fun AnimatedVisibilityScope.NowPlayingExpanded(
    media: PlayingMedia,
    tint: Color,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (positionMs: Long) -> Unit,
    onPlayQueueItem: (id: Long) -> Unit,
    onOpenOutput: () -> Unit,
) {
    val spatial = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        MediaArt(
            media.art,
            EXPANDED_ART,
            Modifier.animateEnterExit(
                enter = scaleIn(spatial, initialScale = ART_ENTER_SCALE),
                exit = scaleOut(spatial, targetScale = ART_ENTER_SCALE),
            ),
        )
        Spacer(Modifier.height(INSET))
        MediaTitle(media, large = true, modifier = Modifier.fillMaxWidth())
        media.progress?.let { progress ->
            Spacer(Modifier.height(GAP))
            WavySeekBar(
                positionMs = rememberPlaybackPosition(progress),
                durationMs = progress.durationMs,
                isPlaying = progress.isAdvancing,
                onSeek = onSeek.takeIf { media.canSeek },
                color = LocalContentColor.current,
            )
        }
        Spacer(Modifier.height(GAP))
        MediaTransport(media, tint, onTogglePlay, onSkipNext, onSkipPrevious)
        media.output?.let {
            Spacer(Modifier.height(GAP))
            OutputChip(it, onOpenOutput)
        }
        if (media.upNext.isNotEmpty()) {
            Spacer(Modifier.height(GAP))
            UpNextList(media.upNext, onPlayQueueItem)
        }
    }
}

/** The card's inset, used again between artwork and text so every edge matches. */
private val INSET = 16.dp
private val GAP = 12.dp

private val COLLAPSED_ART = 56.dp
private val EXPANDED_ART = 160.dp

/** How small the expanded artwork starts as it grows in. */
private const val ART_ENTER_SCALE = 0.6f
