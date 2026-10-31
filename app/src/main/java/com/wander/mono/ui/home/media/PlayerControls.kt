package com.wander.mono.ui.home.media

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.ui.components.WavySeekBar
import com.wander.mono.ui.home.MediaTransport
import com.wander.mono.ui.home.UpNextLine

/**
 * What the expanded player adds under its first line, centred on one axis: the wavy seek bar, the
 * connected transport and, on one line, what plays next. The next line is the first to go: left
 * out when [compact] and whenever it would not fit the room home leaves the player, so the player
 * never squeezes what is under it. Parts come and go with the session (the queue moves): the
 * column resizes on the motion scheme instead of in one frame.
 */
@Composable
internal fun PlayerControls(media: PlayingMedia, tint: Color, compact: Boolean, controls: MediaControls) {
    val next = media.upNext.firstOrNull()?.takeUnless { compact }
    YieldingTail(
        modifier = Modifier.fillMaxWidth().animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec()),
        main = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
            }
        },
        tail = next?.let { entry ->
            @Composable { UpNextLine(entry, controls::playQueueItem, Modifier.padding(top = GAP)) }
        },
    )
}

/**
 * [main], then [tail] under it only while both fit the height offered; otherwise [tail] is not
 * placed, so a short window loses the optional line instead of clipping the transport.
 */
@Composable
private fun YieldingTail(main: @Composable () -> Unit, tail: (@Composable () -> Unit)?, modifier: Modifier = Modifier) {
    Layout(
        content = {
            Box { main() }
            tail?.let { Box { it() } }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val head = measurables[0].measure(loose)
        val rest = measurables.getOrNull(1)
            ?.measure(loose.copy(maxHeight = Constraints.Infinity))
            ?.takeIf { !constraints.hasBoundedHeight || head.height + it.height <= constraints.maxHeight }
        val width = maxOf(head.width, rest?.width ?: 0).coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = (head.height + (rest?.height ?: 0)).coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(width, height) {
            head.place(0, 0)
            rest?.place(0, head.height)
        }
    }
}

private val GAP = 8.dp
