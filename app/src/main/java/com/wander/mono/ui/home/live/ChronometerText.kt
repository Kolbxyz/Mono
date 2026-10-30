package com.wander.mono.ui.home.live

import android.text.format.DateUtils
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.wander.mono.R
import com.wander.mono.core.notifications.Chronometer
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * A notification's chronometer, as the shade's `Chronometer` draws it ("04:59", "1:02:03", a minus
 * once a countdown has passed zero). Computed from its base, never stored: it redraws once per
 * shown second, sleeping until exactly that second, and only while its row is composed (on screen)
 * and home is started. Off screen nothing runs.
 */
@Composable
internal fun ChronometerText(chronometer: Chronometer, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(chronometer, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                now = System.currentTimeMillis()
                delay(msUntilNextSecond(chronometer.base, now))
            }
        }
    }
    val seconds = chronometerSeconds(chronometer.base, now, chronometer.countsDown)
    val shown = DateUtils.formatElapsedTime(abs(seconds))
    Text(
        if (seconds < 0) stringResource(R.string.live_time_negative, shown) else shown,
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color,
        maxLines = 1,
        modifier = modifier,
    )
}
