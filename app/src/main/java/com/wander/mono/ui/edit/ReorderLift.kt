package com.wander.mono.ui.edit

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex

/**
 * A movable row's lift: above its siblings and [ReorderState.displacement] off its slot while
 * [lifted], so it stays under the finger, then easing into the slot with [settle].
 */
internal fun Modifier.reorderLift(lifted: Boolean, reorder: ReorderState, settle: DropSettle): Modifier =
    zIndex(if (lifted || settle.running) 1f else 0f)
        .graphicsLayer { translationY = if (lifted) reorder.displacement else settle.offset }
