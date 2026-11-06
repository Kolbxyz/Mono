package com.wander.mono.ui.drawer

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.theme.LocalReducedMotion
import kotlinx.coroutines.delay

/**
 * The drawer list's entrance: rows composed while the list is [opening] rise into place one after
 * another as the drawer comes up; rows that come in later, by scrolling, are simply there. Not
 * snapshot state: each row reads it once, when it is first composed.
 */
@Stable
internal class ListEntrance {
    var opening = true
        internal set
}

/**
 * The entrance of a list shown now, its opening window closing a moment after it first composes;
 * without [opening], rows are simply there from the start.
 */
@Composable
internal fun rememberListEntrance(opening: Boolean = true): ListEntrance {
    val entrance = remember { ListEntrance().apply { this.opening = opening } }
    LaunchedEffect(entrance) {
        delay(OPENING_WINDOW_MS)
        entrance.opening = false
    }
    return entrance
}

/**
 * How the row at [order] down the list comes in: during the [entrance]'s opening it starts a little
 * low and clear, and rises and fades in on the scheme's default spatial spring, after the rows above
 * it by a short step (capped, so a row far down waits no longer than a few). With reduced motion, or
 * after the opening, it is just there.
 */
@Composable
internal fun entranceMotion(entrance: ListEntrance, order: Int): Modifier {
    val animated = !LocalReducedMotion.current && entrance.opening
    val progress = remember { Animatable(if (animated) 0f else 1f) }
    val spec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val rise = with(LocalDensity.current) { ENTRANCE_RISE.toPx() }
    LaunchedEffect(progress) {
        if (progress.value < 1f) {
            delay(order.coerceIn(0, MAX_STAGGERED) * STAGGER_STEP_MS)
            progress.animateTo(1f, spec)
        }
    }
    return Modifier.graphicsLayer {
        val shown = progress.value
        alpha = shown.coerceIn(0f, 1f)
        translationY = (1f - shown) * rise
    }
}

/** How long after the list first composes its rows still make an entrance. */
private const val OPENING_WINDOW_MS = 400L

/** The delay between one row's entrance and the next. */
private const val STAGGER_STEP_MS = 24L

/** Rows past this many come in together with the last staggered one. */
private const val MAX_STAGGERED = 10

/** How far below its place a row starts its entrance. */
private val ENTRANCE_RISE = 24.dp
