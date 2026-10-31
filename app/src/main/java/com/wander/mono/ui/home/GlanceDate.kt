package com.wander.mono.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.wander.mono.R
import com.wander.mono.data.settings.GlanceLines
import com.wander.mono.ui.theme.secondaryTextColor
import java.util.Locale

/**
 * The date under the clock, when [lines] asks for it, in the user's date style; a tap opens the
 * calendar on today. The rest of the glance is pills ([GlancePills]). [taps] null (the settings
 * preview) makes it plain text.
 */
@Composable
internal fun GlanceDate(glance: Glance, lines: GlanceLines, taps: GlanceTaps?, locale: Locale) {
    if (!lines.date) return
    val style = glance.look.date
    val date = remember(glance.now, style, locale) { format(glance.now, style.skeleton, locale) }
    Text(
        date,
        style = MaterialTheme.typography.titleMedium.onWallpaper(),
        color = secondaryTextColor(),
        modifier = Modifier.tapTo(
            taps?.onDate?.let { open -> { open(glance.now) } },
            R.string.glance_open_calendar,
        ),
    )
}

/**
 * The clock's tap (the alarm list) and long press (its own sheet, [onLongPress]). With a long press
 * it takes the touch, so home's options under it never open as well; without one it is a plain
 * [tapTo].
 */
@Composable
internal fun Modifier.clockPresses(onTap: (() -> Unit)?, onLongPress: (() -> Unit)?): Modifier {
    if (onTap == null || onLongPress == null) return tapTo(onTap, R.string.glance_open_clock)
    return combinedClickable(
        interactionSource = null,
        indication = null,
        onClickLabel = stringResource(R.string.glance_open_clock),
        role = Role.Button,
        onLongClickLabel = stringResource(R.string.glance_clock_options),
        onLongClick = onLongPress,
        onClick = onTap,
    )
}

/**
 * A plain tap on a header line. Not `clickable` nor `detectTapGestures`: both take the touch down,
 * and home's long press (home options) under the clock would never start. This only watches: a
 * quick lift opens, a long press or a scroll leaves the touch to home.
 */
@Composable
internal fun Modifier.tapTo(onTap: (() -> Unit)?, @StringRes label: Int): Modifier {
    if (onTap == null) return this
    val description = stringResource(label)
    return pointerInput(onTap) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val up = waitForUpOrCancellation() ?: return@awaitEachGesture
            if (up.uptimeMillis - down.uptimeMillis < viewConfiguration.longPressTimeoutMillis) {
                up.consume()
                onTap()
            }
        }
    }
        .semantics {
            role = Role.Button
            onClick(description) {
                onTap()
                true
            }
        }
}
