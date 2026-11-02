package com.wander.mono.ui.home.media

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.wander.mono.core.media.PlaybackProgress
import kotlinx.coroutines.delay

/**
 * The playback position now, as a lambda reading a ticking clock: the session reports position
 * only on changes, so while playing it is extrapolated once per second of playback (when the
 * seconds label changes), only while home is visible. Never on the frame clock: that would redraw
 * home on every frame for as long as music plays. Whoever calls the lambda is what recomposes or
 * redraws on each tick.
 */
@Composable
internal fun rememberPlaybackPosition(progress: PlaybackProgress): () -> Long {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(progress, lifecycle) {
        now = SystemClock.elapsedRealtime()
        if (!progress.isAdvancing) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                now = SystemClock.elapsedRealtime()
                delay(progress.msToNextSecondAt(now))
            }
        }
    }
    return remember(progress) { { progress.positionAt(now) } }
}

/** The position at [nowMs] on the elapsed-realtime clock, clamped to the track. */
private fun PlaybackProgress.positionAt(nowMs: Long): Long {
    val elapsed = if (isAdvancing) ((nowMs - updatedAtMs).coerceAtLeast(0L) * speed).toLong() else 0L
    return (positionMs + elapsed).coerceIn(0L, durationMs)
}
