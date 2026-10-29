package com.wander.mono.ui.home

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import com.wander.mono.core.media.PlaybackProgress

/**
 * The wavy bar of the expanded card, draggable to seek. Grabbing it (past the touch slop, so a
 * vertical scroll still scrolls) ticks a haptic and takes the gesture from the card's skip swipe;
 * the bar follows the finger and the session seeks on release. The dragged position stays until
 * the session reports the new one, so the bar does not jump back in between.
 */
@Composable
internal fun SeekablePlaybackBar(
    progress: PlaybackProgress,
    color: Color,
    onSeek: (positionMs: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val currentOnSeek by rememberUpdatedState(onSeek)
    var scrub by remember { mutableStateOf<Float?>(null) }
    var dragging by remember { mutableStateOf(false) }
    LaunchedEffect(progress) { if (!dragging) scrub = null }

    fun seek(fraction: Float) = currentOnSeek((fraction * progress.durationMs).toLong())

    PlaybackBar(
        progress = progress,
        color = color,
        scrub = scrub,
        modifier = modifier
            .semantics {
                setProgress { target ->
                    seek(target.coerceIn(0f, 1f))
                    true
                }
            }
            .pointerInput(progress.durationMs) {
                fun fractionAt(x: Float) = (x / size.width).coerceIn(0f, 1f)
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val grabbed = awaitHorizontalTouchSlopOrCancellation(down.id) { change, _ ->
                        change.consume()
                    } ?: return@awaitEachGesture
                    haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    dragging = true
                    scrub = fractionAt(grabbed.position.x)
                    val completed = horizontalDrag(grabbed.id) { change ->
                        scrub = fractionAt(change.position.x)
                        change.consume()
                    }
                    dragging = false
                    val released = scrub
                    if (completed && released != null) seek(released) else scrub = null
                }
            }
            .padding(vertical = GRAB_SLACK),
    )
}

/** Touch room above and below the thin bar, so it can be grabbed without precision. */
private val GRAB_SLACK = 12.dp
