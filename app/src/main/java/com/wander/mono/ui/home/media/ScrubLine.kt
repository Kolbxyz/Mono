package com.wander.mono.ui.home.media

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.media.PlaybackProgress
import com.wander.mono.ui.components.WavyProgressLine
import com.wander.mono.ui.components.rememberMonoHaptics

/**
 * The strip's thin wavy progress line. When the session can seek ([onSeek]), dragging along it
 * scrubs: the line follows the finger (the wave stills) and the release seeks there. The drag is
 * caught before the card's swipe to skip, and TalkBack can set the position directly.
 */
@Composable
internal fun ScrubLine(
    progress: PlaybackProgress,
    onSeek: ((positionMs: Long) -> Unit)?,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val position = rememberPlaybackPosition(progress)
    val duration = progress.durationMs
    var scrub by remember { mutableStateOf<Float?>(null) }
    val seek by rememberUpdatedState(onSeek)
    val haptics = rememberMonoHaptics()
    val description = stringResource(R.string.media_progress)
    val shown = (progress.positionMs.toFloat() / duration).coerceIn(0f, 1f)
    val scrubbing = if (onSeek == null) {
        Modifier
    } else {
        Modifier.pointerInput(duration) {
            detectHorizontalDragGestures(
                onDragStart = { scrub = (it.x / size.width).coerceIn(0f, 1f) },
                onDragEnd = {
                    scrub?.let { fraction -> seek?.invoke((fraction * duration).toLong()) }
                    scrub = null
                    haptics.gestureEnd()
                },
                onDragCancel = { scrub = null },
            ) { change, _ ->
                change.consume()
                scrub = (change.position.x / size.width).coerceIn(0f, 1f)
            }
        }
    }
    Box(
        modifier
            .fillMaxWidth()
            .height(TOUCH_HEIGHT)
            .then(scrubbing)
            .semantics {
                contentDescription = description
                progressBarRangeInfo = ProgressBarRangeInfo(shown, 0f..1f)
                if (onSeek != null) {
                    setProgress { target ->
                        onSeek((target.coerceIn(0f, 1f) * duration).toLong())
                        true
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        WavyProgressLine(
            fraction = { scrub ?: (position().toFloat() / duration).coerceIn(0f, 1f) },
            isPlaying = progress.isAdvancing && scrub == null,
            color = color,
        )
    }
}

/** Taller than the line it holds, so the finger finds it. */
private val TOUCH_HEIGHT = 20.dp
