package com.wander.mono.ui.drawer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.components.rememberMonoHaptics

/**
 * Counts one gesture's upward drag that the list could not take (it has no more to scroll), and
 * says when it passes [threshold] pixels: once per gesture, and only while the list itself has not
 * moved since, so a scroll that runs into the end does not count what came before it.
 */
internal class SearchPull(private val threshold: Float) {
    private var pulled = 0f
    private var fired = false

    /**
     * One step of the drag: [consumed] by the list, [leftover] after it (negative is up). True on
     * the step that passes the threshold.
     */
    fun drag(consumed: Float, leftover: Float): Boolean {
        if (consumed != 0f || leftover > 0f) {
            pulled = 0f
            return false
        }
        pulled -= leftover
        if (fired || pulled < threshold) return false
        fired = true
        return true
    }

    /** The finger lifted: the next gesture counts afresh. */
    fun end() {
        pulled = 0f
        fired = false
    }
}

/**
 * The drawer list's second swipe up: dragging up once the list has nowhere further to go (all of it
 * fits, or it is at its end) runs [onPull] once per gesture, with a tick, after a short distance.
 * The drag is left to the list's own overscroll stretch, which shows the pull.
 */
@Composable
internal fun rememberSearchPullConnection(onPull: () -> Unit): NestedScrollConnection {
    val threshold = with(LocalDensity.current) { PULL_DISTANCE.toPx() }
    val haptics = rememberMonoHaptics()
    val currentOnPull = rememberUpdatedState(onPull)
    return remember(threshold, haptics) {
        val pull = SearchPull(threshold)
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && pull.drag(consumed.y, available.y)) {
                    haptics.threshold()
                    currentOnPull.value()
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                pull.end()
                return Velocity.Zero
            }
        }
    }
}

/** How far past the list's end an upward drag goes before it opens search. */
private val PULL_DISTANCE = 72.dp
