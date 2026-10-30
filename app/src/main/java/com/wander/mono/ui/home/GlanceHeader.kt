package com.wander.mono.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.ClockAlignment
import com.wander.mono.ui.theme.secondaryTextColor
import android.text.format.DateFormat as AndroidDateFormat

/**
 * Big clock, date, next alarm and next calendar event, formatted for the user's locale and 12/24-hour
 * setting, styled and filtered by the user's clock settings. Each line opens what it names.
 * [compact] (a phone in landscape) trades the airy spacing and display size for list room.
 */
@Composable
fun GlanceHeader(
    glance: Glance,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    actions: GlanceActionsViewModel = viewModel(),
) {
    val taps = remember(actions) { actions.taps() }
    GlanceContent(glance, taps, modifier, compact)
}

/** The header itself; [taps] null (the settings preview) makes every line plain text. */
@Composable
fun GlanceContent(glance: Glance, taps: GlanceTaps?, modifier: Modifier = Modifier, compact: Boolean = false) {
    val locale = LocalConfiguration.current.locales[0]
    // Re-read on every minute tick; flipping the setting also sends ACTION_TIME_CHANGED.
    val is24Hour = AndroidDateFormat.is24HourFormat(LocalContext.current)
    val timeSkeleton = if (is24Hour) "Hm" else "hm"
    val look = glance.look
    val lines = look.lines
    val centred = look.alignment == ClockAlignment.Center
    // Lines and chips come and go (an alarm set, quiet started): the header resizes on the motion
    // scheme instead of in one frame, and the list under it follows.
    Column(
        modifier
            .padding(top = if (compact) 8.dp else 48.dp, bottom = if (compact) 16.dp else 32.dp)
            .roomyContentSize(MaterialTheme.motionScheme.defaultSpatialSpec(), SHADOW_ROOM),
        horizontalAlignment = if (centred) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        ClockText(
            glance.now,
            is24Hour,
            look,
            locale,
            compact,
            Modifier.tapTo(taps?.onTime, R.string.glance_open_clock),
        )
        val lineStyle = MaterialTheme.typography.titleMedium.onWallpaper()
        if (lines.date) {
            val date = remember(glance.now, look.date, locale) { format(glance.now, look.date.skeleton, locale) }
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
            Text(
                stringResource(R.string.home_next_alarm, alarm),
                style = lineStyle,
                color = secondaryTextColor(),
                modifier = Modifier.tapTo(taps?.onAlarm, R.string.glance_open_alarms),
            )
        }
        val battery = glance.battery?.takeIf { lines.battery }
        val quiet = glance.quiet
        if (battery != null || quiet != null) {
            // Wraps at large font sizes rather than pushing the last chip off the screen.
            FlowRow(
                Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                quiet?.let { QuietChip(it, timeSkeleton, taps?.onEndQuiet, taps?.onExtendQuiet) }
                battery?.let { BatteryChip(it) }
            }
        }
    }
}

/**
 * A plain tap on a header line. Not `clickable` nor `detectTapGestures`: both take the touch down,
 * and home's long press (home options) under the clock would never start. This only watches: a
 * quick lift opens, a long press or a scroll leaves the touch to home.
 */
@Composable
private fun Modifier.tapTo(onTap: (() -> Unit)?, @StringRes label: Int): Modifier {
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

/** Room past the header's edges for the text shadow or halo, which a size animation's clip would cut. */
private val SHADOW_ROOM = 16.dp
