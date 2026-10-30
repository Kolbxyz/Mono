package com.wander.mono.ui.drawer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Whether the drawer's toolbar shows, from how the list scrolls: it gets out of the way after a
 * short scroll toward the end and comes back after as much toward the start. It only watches what
 * the list consumed, never consuming anything itself.
 */
@Stable
internal class ToolbarScroll(private val threshold: Float) : NestedScrollConnection {
    var visible by mutableStateOf(true)
        private set
    private var travel = 0f

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (consumed.y != 0f) {
            travel = ToolbarMath.travel(travel, consumed.y)
            visible = ToolbarMath.visible(visible, travel, threshold)
        }
        return Offset.Zero
    }
}

@Composable
internal fun rememberToolbarScroll(): ToolbarScroll {
    val threshold = with(LocalDensity.current) { TOOLBAR_SCROLL_THRESHOLD.toPx() }
    return remember(threshold) { ToolbarScroll(threshold) }
}

/** Scroll deltas are negative toward the end of the list (the finger moving up), positive back. */
internal object ToolbarMath {
    /** Scrolling on the same way adds up; turning back starts over from this [delta]. */
    fun travel(previous: Float, delta: Float): Float = if (previous * delta < 0f) delta else previous + delta

    /** Hidden once [travel] passes [threshold] toward the end, shown once it passes it back. */
    fun visible(current: Boolean, travel: Float, threshold: Float): Boolean = when {
        travel <= -threshold -> false
        travel >= threshold -> true
        else -> current
    }
}

private val TOOLBAR_SCROLL_THRESHOLD = 24.dp
