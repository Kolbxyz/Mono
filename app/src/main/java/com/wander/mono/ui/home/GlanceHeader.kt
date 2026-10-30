package com.wander.mono.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.ClockAlignment
import com.wander.mono.data.settings.GlanceLines
import android.text.format.DateFormat as AndroidDateFormat

/**
 * Big clock, date, next alarm and next calendar event, formatted for the user's locale and 12/24-hour
 * setting, styled and filtered by the user's clock settings. Each line opens what it names.
 * [compact] (a phone in landscape) trades the airy spacing and display size for list room;
 * [reachable] sits it low, over the search dock. [hero] is how far the hero pull has grown it.
 */
@Composable
fun GlanceHeader(
    glance: Glance,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    reachable: Boolean = false,
    simple: Boolean = false,
    hero: () -> Float = { 0f },
    actions: GlanceActionsViewModel = viewModel(),
    alarms: AlarmDismissViewModel = viewModel(),
) {
    val taps = remember(actions) { actions.taps() }
    GlanceContent(glance, taps, modifier, compact, reachable, simple, hero, alarms::dismissNext)
}

/**
 * The header itself; [taps] null (the settings preview) makes every line plain text. The large,
 * simple preset ([simple]) keeps only the clock and date; the hero's summary brings in the rest.
 */
@Composable
fun GlanceContent(
    glance: Glance,
    taps: GlanceTaps?,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    reachable: Boolean = false,
    simple: Boolean = false,
    hero: () -> Float = { 0f },
    onDismissAlarm: (() -> Unit)? = null,
) {
    val locale = LocalConfiguration.current.locales[0]
    // Re-read on every minute tick; flipping the setting also sends ACTION_TIME_CHANGED.
    val is24Hour = AndroidDateFormat.is24HourFormat(LocalContext.current)
    val timeSkeleton = if (is24Hour) "Hm" else "hm"
    val look = glance.look
    val lines = shownLines(look.lines, simple)
    val centred = look.alignment == ClockAlignment.Center
    val (top, bottom) = when {
        compact -> 8.dp to 16.dp
        reachable -> 24.dp to 16.dp
        else -> 48.dp to 32.dp
    }
    Column(
        modifier.padding(top = top, bottom = bottom),
        horizontalAlignment = if (centred) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        // Outside the animated size below: the hero pull changes the clock's height every frame,
        // which must follow the finger, not chase it.
        ClockText(
            glance.now,
            is24Hour,
            look,
            locale,
            compact,
            Modifier
                .heroGrow(hero, centred)
                .tapTo(taps?.onTime, R.string.glance_open_clock),
        )
        // Lines and chips come and go (an alarm set, quiet started): the header resizes on the
        // motion scheme instead of in one frame, and the list under it follows.
        Column(
            Modifier.roomyContentSize(MaterialTheme.motionScheme.defaultSpatialSpec(), SHADOW_ROOM),
            horizontalAlignment = if (centred) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            GlanceLineList(glance, lines, taps, onDismissAlarm, timeSkeleton, locale)
            GlanceChips(glance, lines.battery, timeSkeleton, taps)
        }
        HeroSummary(glance, summaryLines(lines), hero, timeSkeleton, taps, onDismissAlarm, centred)
    }
}

/** Quiet mode and the battery, when [battery] is asked for, wrapping at large font sizes. */
@Composable
private fun GlanceChips(glance: Glance, battery: Boolean, timeSkeleton: String, taps: GlanceTaps?) {
    val power = glance.battery?.takeIf { battery }
    val quiet = glance.quiet
    if (power == null && quiet == null) return
    // Wraps at large font sizes rather than pushing the last chip off the screen.
    FlowRow(
        Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        quiet?.let { QuietChip(it, timeSkeleton, taps?.onEndQuiet, taps?.onExtendQuiet) }
        power?.let { BatteryChip(it) }
    }
}

/**
 * The hero's "today" summary: the [lines] the header is not showing, composed only while the hero
 * pull is out, opening as the clock grows.
 */
@Composable
private fun HeroSummary(
    glance: Glance,
    lines: GlanceLines,
    hero: () -> Float,
    timeSkeleton: String,
    taps: GlanceTaps?,
    onDismissAlarm: (() -> Unit)?,
    centred: Boolean,
) {
    val out by remember(hero) { derivedStateOf { hero() > 0f } }
    if (!out) return
    val locale = LocalConfiguration.current.locales[0]
    Column(
        Modifier.heroReveal(hero).padding(top = 8.dp),
        horizontalAlignment = if (centred) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        GlanceLineList(glance, lines, taps, onDismissAlarm, timeSkeleton, locale)
        glance.battery?.takeIf { lines.battery }?.let { BatteryChip(it, Modifier.padding(top = 8.dp)) }
    }
}

/** Room past the header's edges for the text shadow or halo, which a size animation's clip would cut. */
private val SHADOW_ROOM = 16.dp
