package com.wander.mono.ui.edit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

/**
 * The rest of a drop: where the finger left a row, relative to its slot, eased back to the slot
 * instead of snapping there when the drag ends.
 */
@Stable
internal class DropSettle(private val scope: CoroutineScope) {
    private val residual = Animatable(0f)

    val offset: Float get() = residual.value

    val running: Boolean get() = residual.isRunning

    /** Starts at once (not on the next frame), so the row is never drawn in its slot in between. */
    fun release(from: Float, spec: FiniteAnimationSpec<Float>) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            residual.snapTo(from)
            residual.animateTo(0f, spec)
        }
    }
}

@Composable
internal fun rememberDropSettle(): DropSettle {
    val scope = rememberCoroutineScope()
    return remember(scope) { DropSettle(scope) }
}

/**
 * A movable row's lift: above its siblings and [ReorderState.displacement] off its slot while
 * [lifted], so it stays under the finger, then easing into the slot with [settle].
 */
internal fun Modifier.reorderLift(lifted: Boolean, reorder: ReorderState, settle: DropSettle): Modifier =
    zIndex(if (lifted || settle.running) 1f else 0f)
        .graphicsLayer { translationY = if (lifted) reorder.displacement else settle.offset }
