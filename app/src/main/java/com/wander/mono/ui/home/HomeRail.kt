package com.wander.mono.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.drawer.LetterRail
import com.wander.mono.ui.drawer.Section

/**
 * [state]'s rail down home's end edge (the start edge [atStart]), from under the status bar to just
 * above [bottomClearance]. It grows out of the thumb as a finger lands ([LetterRail]'s reveal: the
 * letters nearest the finger first), on the scheme's effects spec, and leaves the same way.
 */
@Composable
fun BoxScope.HomeRail(
    state: HomeRailState,
    sections: List<Section>,
    atStart: Boolean,
    bottomClearance: Dp,
    onLand: () -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    val active = state.scrub.active
    val reveal = animateFloatAsState(
        if (active) 1f else 0f,
        if (active) motion.defaultEffectsSpec() else motion.fastEffectsSpec(),
        label = "homeRail",
    )
    val present by remember(state) { derivedStateOf { state.scrub.active || reveal.value > 0f } }
    if (!present) return
    LetterRail(
        sections = sections,
        current = state.held.coerceAtLeast(0),
        // Home has no list to jump: the held letter is all it shows.
        onSelect = {},
        modifier = Modifier
            .align(if (atStart) Alignment.CenterStart else Alignment.CenterEnd)
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
            .padding(top = RAIL_TOP, bottom = bottomClearance)
            .fillMaxHeight(),
        atStart = atStart,
        scrub = state.scrub,
        onHold = { state.hold(it, sections.size, onLand) },
        reveal = { reveal.value },
    )
}

/**
 * Home's header, list and button giving way while [state] shows a letter instead: they fade and
 * sink back a little, quickly, so the letter's apps take the stage; and come back, a touch slower,
 * when the finger returns to ★. Read in the draw phase only: nothing recomposes as they move.
 */
@Composable
fun Modifier.givesWayToHeldLetter(state: HomeRailState): Modifier {
    val motion = MaterialTheme.motionScheme
    val away = state.shown > 0
    val fade = animateFloatAsState(
        if (away) 0f else 1f,
        if (away) motion.fastEffectsSpec() else motion.defaultEffectsSpec(),
        label = "homeAway",
    )
    val scale = animateFloatAsState(if (away) AWAY_SCALE else 1f, motion.fastSpatialSpec(), label = "homeSink")
    return graphicsLayer {
        alpha = fade.value
        scaleX = scale.value
        scaleY = scale.value
    }
}

/** How far home sinks back while a letter takes its place. */
private const val AWAY_SCALE = 0.96f

/** Air between the status bar and ★. */
private val RAIL_TOP = 8.dp
