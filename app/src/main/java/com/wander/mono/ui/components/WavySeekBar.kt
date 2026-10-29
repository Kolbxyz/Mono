package com.wander.mono.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.theme.textAlpha
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs

/**
 * The Expressive wavy seek bar, after Wanda's player: a slider whose played side waves while
 * [isPlaying], whose unplayed side is a thin pill, and whose dot thumb morphs into an upright bar
 * under the finger, the wave flattening so the thumb has a straight edge to aim along. Dragging
 * ticks a haptic every few seconds of track. The released position is held until [positionMs]
 * catches up, so the thumb does not fall back for the frames before the player reports the seek.
 *
 * [positionMs] is a lambda so a ticking clock recomposes only this bar. A null [onSeek] shows the
 * same bar without a thumb, for sessions that cannot seek. Elapsed and remaining time sit under the
 * track's two ends, in [color] like the rest.
 */
@Composable
fun WavySeekBar(
    positionMs: () -> Long,
    durationMs: Long,
    isPlaying: Boolean,
    onSeek: ((positionMs: Long) -> Unit)?,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val currentPosition by rememberUpdatedState(positionMs)
    var scrub by remember { mutableFloatStateOf(NONE) }
    var pending by remember { mutableFloatStateOf(NONE) }
    var lastTick by remember { mutableIntStateOf(-1) }
    val seekable = onSeek != null && durationMs > 0L
    val scrubbing = scrub >= 0f
    val position = positionMs()
    val fraction = when {
        scrubbing -> scrub
        pending >= 0f -> pending
        durationMs > 0L -> (position.toFloat() / durationMs).coerceIn(0f, 1f)
        else -> 0f
    }

    // Stop holding once the player reports the seek, or after a while if it evidently never will.
    LaunchedEffect(pending) {
        if (pending < 0f) return@LaunchedEffect
        val target = pending * durationMs
        withTimeoutOrNull(SETTLE_TIMEOUT_MS) {
            snapshotFlow { currentPosition() }.first { abs(it - target) < SETTLE_TOLERANCE_MS }
        }
        pending = NONE
    }

    val amplitude = rememberWaveAmplitude(isPlaying && !scrubbing)
    val spatial = MaterialTheme.motionScheme.fastSpatialSpec<Dp>()
    val thumbWidth by animateDpAsState(if (scrubbing) SCRUB_THUMB_WIDTH else RESTING_THUMB, spatial)
    val thumbHeight by animateDpAsState(if (scrubbing) SCRUB_THUMB_HEIGHT else RESTING_THUMB, spatial)
    val description = stringResource(R.string.media_progress)

    Column(modifier.fillMaxWidth()) {
        Slider(
            value = fraction,
            onValueChange = { value ->
                scrub = value
                val interval = (value * durationMs / TICK_EVERY_MS).toInt()
                if (interval != lastTick) {
                    lastTick = interval
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                }
            },
            onValueChangeFinished = {
                val released = scrub
                if (released >= 0f && onSeek != null) {
                    haptics.performHapticFeedback(HapticFeedbackType.GestureEnd)
                    pending = released
                    onSeek((released * durationMs).toLong())
                }
                scrub = NONE
                lastTick = -1
            },
            enabled = seekable,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = description },
            thumb = {
                Box(Modifier.size(THUMB_SLOT), contentAlignment = Alignment.Center) {
                    if (seekable) SeekThumb(thumbWidth, thumbHeight, color)
                }
            },
            track = { state ->
                SeekTrack(state.value, amplitude, if (seekable) thumbWidth else 0.dp, color)
            },
        )
        // Inset to the track's ends, not the slider's: the slider keeps half a thumb slot clear each side.
        val shown = if (scrubbing || pending >= 0f) (fraction * durationMs).toLong() else position
        val labelColor = color.copy(alpha = textAlpha(LABEL_ALPHA))
        val style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum")
        Row(Modifier.fillMaxWidth().padding(horizontal = THUMB_SLOT / 2)) {
            Text(playbackTime(shown), style = style, color = labelColor, modifier = Modifier.weight(1f))
            Text(
                stringResource(R.string.media_time_remaining, playbackTime(durationMs - shown)),
                style = style,
                color = labelColor,
            )
        }
    }
}

/** [ms] as `m:ss`, or `h:mm:ss` from an hour. */
@Composable
private fun playbackTime(ms: Long): String {
    val total = ms.coerceAtLeast(0L) / MS_PER_SECOND
    val hours = total / SECONDS_PER_HOUR
    val minutes = total % SECONDS_PER_HOUR / SECONDS_PER_MINUTE
    val seconds = total % SECONDS_PER_MINUTE
    return if (hours > 0L) {
        stringResource(R.string.media_time_hours, hours, minutes, seconds)
    } else {
        stringResource(R.string.media_time_minutes, minutes, seconds)
    }
}

/** No scrub or pending seek. */
private const val NONE = -1f

/**
 * The box the thumb is laid out in, sized for the largest the thumb gets, so its morph never
 * changes the slider's measurement and shifts the track.
 */
private val THUMB_SLOT = 24.dp
private val RESTING_THUMB = 14.dp
private val SCRUB_THUMB_WIDTH = 6.dp
private val SCRUB_THUMB_HEIGHT = 24.dp

/** A haptic tick for every this much track dragged past. */
private const val TICK_EVERY_MS = 15_000L

/** How close the position must land to the seek target to count as caught up. */
private const val SETTLE_TOLERANCE_MS = 400L

/** How long to hold the released position before giving up on the player confirming it. */
private const val SETTLE_TIMEOUT_MS = 1_500L

/** The time labels, dimmer than the bar in the same colour. */
private const val LABEL_ALPHA = 0.74f

private const val MS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L
private const val SECONDS_PER_HOUR = 3_600L
