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
import com.wander.mono.core.system.canOfferAlarmDismiss
import com.wander.mono.data.settings.GlanceLines
import com.wander.mono.ui.theme.secondaryTextColor
import java.util.Locale

/**
 * The header's text lines that [lines] asks for and [glance] has: the date, the next event, the next
 * alarm. Each opens what it names; a long press on the alarm dismisses it ([onDismissAlarm]) when
 * [canOfferAlarmDismiss] says the clock app would.
 * [taps] null (the settings preview) makes every line plain text.
 */
@Composable
internal fun GlanceLineList(
    glance: Glance,
    lines: GlanceLines,
    taps: GlanceTaps?,
    onDismissAlarm: (() -> Unit)?,
    timeSkeleton: String,
    locale: Locale,
) {
    val lineStyle = MaterialTheme.typography.titleMedium.onWallpaper()
    if (lines.date) {
        val style = glance.look.date
        val date = remember(glance.now, style, locale) { format(glance.now, style.skeleton, locale) }
        Text(
            date,
            style = lineStyle,
            color = secondaryTextColor(),
            modifier = Modifier.tapTo(
                taps?.onDate?.let { open -> { open(glance.now) } },
                R.string.glance_open_calendar,
            ),
        )
    }
    glance.nextEvent?.takeIf { lines.event }?.let { event ->
        NextEventLine(
            event,
            glance.now,
            timeSkeleton,
            Modifier.tapTo(taps?.onEvent?.let { open -> { open(event) } }, R.string.glance_open_event),
        )
    }
    glance.nextAlarm?.takeIf { lines.alarm }?.let { next ->
        val alarm = remember(next, timeSkeleton, locale) { format(next, ALARM_DAY_SKELETON + timeSkeleton, locale) }
        val onOpen = taps?.onAlarm
        // Only an alarm the clock app would dismiss; otherwise the long press stays home's options.
        val onDismiss = onDismissAlarm?.takeIf { canOfferAlarmDismiss(next, glance.now) }
        Text(
            stringResource(R.string.home_next_alarm, alarm),
            style = lineStyle,
            color = secondaryTextColor(),
            modifier = if (onOpen != null && onDismiss != null) {
                Modifier.alarmPresses(onOpen, onDismiss)
            } else {
                Modifier.tapTo(onOpen, R.string.glance_open_alarms)
            },
        )
    }
}

/**
 * The alarm line's tap (the alarm list) and long press (dismiss the next alarm). Unlike [tapTo] it
 * takes the touch, so its long press is its own and never also opens home's options under it.
 */
@Composable
private fun Modifier.alarmPresses(onOpen: () -> Unit, onDismiss: () -> Unit): Modifier = combinedClickable(
    interactionSource = null,
    indication = null,
    onClickLabel = stringResource(R.string.glance_open_alarms),
    role = Role.Button,
    onLongClickLabel = stringResource(R.string.glance_dismiss_alarm),
    onLongClick = onDismiss,
    onClick = onOpen,
)

/**
 * The clock's tap (the alarm list) and long press (its own sheet, [onLongPress]). With a long press
 * it takes the touch, like [alarmPresses], so home's options under it never open as well; without
 * one it is a plain [tapTo].
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

private const val ALARM_DAY_SKELETON = "EEE"
