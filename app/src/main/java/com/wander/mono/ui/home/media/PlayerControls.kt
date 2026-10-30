package com.wander.mono.ui.home.media

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.ui.components.WavySeekBar
import com.wander.mono.ui.home.MediaTransport
import com.wander.mono.ui.home.OutputChip
import com.wander.mono.ui.home.UpNextList

/**
 * What the expanded player adds under its first line, centred on one axis: the wavy seek bar, the
 * connected transport, where it is heard and what plays next ([compact] leaves the list out, for a
 * phone on its side). Parts come and go with the session (an output appears, the queue moves): the
 * column resizes on the motion scheme instead of in one frame.
 */
@Composable
internal fun PlayerControls(media: PlayingMedia, tint: Color, compact: Boolean, controls: MediaControls) {
    Column(
        Modifier.fillMaxWidth().animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        media.progress?.let { progress ->
            Spacer(Modifier.height(GAP))
            WavySeekBar(
                positionMs = rememberPlaybackPosition(progress),
                durationMs = progress.durationMs,
                isPlaying = progress.isAdvancing,
                onSeek = controls::seekTo.takeIf { media.canSeek },
                color = LocalContentColor.current,
            )
        }
        Spacer(Modifier.height(GAP))
        MediaTransport(media, tint, controls::togglePlayPause, controls::skipNext, controls::skipPrevious)
        media.output?.let {
            Spacer(Modifier.height(GAP))
            OutputChip(it, controls::openOutputSwitcher)
        }
        if (media.upNext.isNotEmpty() && !compact) {
            Spacer(Modifier.height(GAP))
            UpNextList(media.upNext, controls::playQueueItem)
        }
    }
}

private val GAP = 12.dp
