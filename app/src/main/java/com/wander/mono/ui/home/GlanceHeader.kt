package com.wander.mono.ui.home

import androidx.compose.foundation.layout.Column
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
import com.wander.mono.data.settings.ClockAlignment
import com.wander.mono.data.settings.GlanceLines
import android.text.format.DateFormat as AndroidDateFormat

/**
 * Big clock, then the date and the glance pills (next event, next alarm, quiet, battery), formatted
 * for the user's locale and 12/24-hour setting, styled and filtered by the user's clock settings.
 * Each line and pill opens what it names.
 * [compact] (a phone in landscape) trades the airy spacing and display size for list room;
 * [reachable] sits it low, over home's button. [hero] is how far the hero pull has grown it.
 * A long press on the clock runs [onClockOptions]. [player] goes under it all: the player under the clock.
 */
@Composable
fun GlanceHeader(
    glance: Glance,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    reachable: Boolean = false,
    simple: Boolean = false,
    hero: () -> Float = { 0f },
    onClockOptions: (() -> Unit)? = null,
    player: @Composable () -> Unit = {},
    actions: GlanceActionsViewModel = viewModel(),
    alarms: AlarmDismissViewModel = viewModel(),
) {
    val taps = remember(actions) { actions.taps() }
    GlanceContent(glance, taps, modifier, compact, reachable, simple, hero, alarms::dismissNext, onClockOptions, player)
}

/**
 * The header itself; [taps] null (the settings preview) makes every line plain text. The large,
 * simple preset ([simple]) keeps only the clock and date; the hero's summary brings in the rest.
 * With [onClockOptions], a long press on the clock is the clock's own and never home's options.
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
    onClockOptions: (() -> Unit)? = null,
    player: @Composable () -> Unit = {},
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
        else -> 56.dp to 44.dp
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
                .clockPresses(taps?.onTime, onClockOptions),
        )
        // Lines and chips come and go (an alarm set, quiet started): the header resizes on the
        // motion scheme instead of in one frame, and the list under it follows.
        Column(
            Modifier.roomyContentSize(MaterialTheme.motionScheme.defaultSpatialSpec(), SHADOW_ROOM),
            horizontalAlignment = if (centred) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            GlanceDate(glance, lines, taps, locale)
            GlancePills(glance, lines, withQuiet = true, taps, onDismissAlarm, timeSkeleton, centred)
        }
        HeroSummary(glance, summaryLines(lines), hero, timeSkeleton, taps, onDismissAlarm, centred)
        player()
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
        GlanceDate(glance, lines, taps, locale)
        GlancePills(glance, lines, withQuiet = false, taps, onDismissAlarm, timeSkeleton, centred)
    }
}

/** Room past the header's edges for the text shadow or halo, which a size animation's clip would cut. */
private val SHADOW_ROOM = 16.dp
