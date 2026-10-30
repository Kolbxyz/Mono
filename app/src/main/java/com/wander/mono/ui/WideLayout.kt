package com.wander.mono.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.components.FollowDrag
import com.wander.mono.ui.components.ReadableLayout

/**
 * Whether the window has room for home and the drawer side by side ([ReadableLayout.isWide]),
 * from the window's own size, so split screen and freeform windows count as what they are.
 */
@Composable
fun rememberWideWindow(): Boolean {
    val size = LocalWindowInfo.current.containerSize
    return with(LocalDensity.current) { ReadableLayout.isWide(size.width.toDp(), size.height.toDp()) }
}

/**
 * Home's swipe up in the wide layout, where the drawer already stands beside home and has nowhere
 * to rise: a swipe past [threshold] px, or a flick faster than [fling] px/s, calls [onPull] (bring
 * up the side panel's search) once the finger lifts.
 */
@Stable
class PanelSearchPull internal constructor(
    private val threshold: Float,
    private val fling: Float,
    private val onPull: State<() -> Unit>,
) : FollowDrag {
    private var pulled = 0f
    private var dragging = false

    override val isDragging: Boolean get() = dragging

    override fun dragBy(deltaY: Float) {
        dragging = true
        pulled -= deltaY
    }

    override fun release(velocityY: Float) {
        if (!dragging) return
        dragging = false
        if (pulled >= threshold || -velocityY > fling) onPull.value()
        pulled = 0f
    }
}

@Composable
fun rememberPanelSearchPull(onPull: () -> Unit): PanelSearchPull {
    val current = rememberUpdatedState(onPull)
    val (threshold, fling) = with(LocalDensity.current) { PULL_DISTANCE.toPx() to FLING_VELOCITY.toPx() }
    return remember(threshold, fling) { PanelSearchPull(threshold, fling, current) }
}

private val PULL_DISTANCE = 72.dp

/** Per second, as the drawer's own flick. */
private val FLING_VELOCITY = 400.dp
