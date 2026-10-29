package com.wander.mono.ui.home

import android.os.SystemClock
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.wander.mono.R
import com.wander.mono.core.media.PlaybackProgress
import com.wander.mono.ui.theme.LocalReducedMotion
import kotlinx.coroutines.delay

/**
 * The Expressive wavy progress bar. The session reports position only on changes, so while playing
 * the bar advances on the frame clock (once a second with reduced motion), only while home is
 * visible. The wave flattens when paused and never moves with reduced motion. A [scrub] fraction,
 * while the user drags to seek, is shown instead of the playback position.
 */
@Composable
internal fun PlaybackBar(
    progress: PlaybackProgress,
    color: Color,
    modifier: Modifier = Modifier,
    scrub: Float? = null,
) {
    val reduced = LocalReducedMotion.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(progress, reduced, lifecycle) {
        now = SystemClock.elapsedRealtime()
        if (!progress.isAdvancing) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                if (reduced) delay(REDUCED_TICK_MS) else withFrameMillis { }
                now = SystemClock.elapsedRealtime()
            }
        }
    }
    val wavy = progress.isAdvancing && !reduced
    val description = stringResource(R.string.media_progress)
    LinearWavyProgressIndicator(
        progress = { scrub ?: progress.fractionAt(now) },
        modifier = modifier.fillMaxWidth().semantics { contentDescription = description },
        color = color,
        trackColor = color.copy(alpha = TRACK_ALPHA),
        amplitude = { if (wavy) 1f else 0f },
        waveSpeed = if (reduced) 0.dp else WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
    )
}

private const val REDUCED_TICK_MS = 1_000L

/** The unplayed part, in the card's content colour so it reads on any artwork tint. */
private const val TRACK_ALPHA = 0.24f
