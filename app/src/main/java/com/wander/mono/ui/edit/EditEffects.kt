package com.wander.mono.ui.edit

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.wander.mono.ui.home.HomeRow
import com.wander.mono.ui.home.folders.flattenFolders

/**
 * What edit mode keeps in step with the list: the dragged order is dropped once saved (or when
 * edit mode ends), and while a row is dragged past an edge of the list, the list scrolls, once
 * per frame, only for as long as the finger holds it there.
 */
@Composable
fun EditEffects(editing: HomeEditing, rows: List<HomeRow>?, list: LazyListState) {
    val reorder = editing.reorder
    val haptics = LocalHapticFeedback.current
    val dragging = reorder.dragging != null
    LaunchedEffect(rows, dragging) {
        reorder.settle(flattenFolders(rows.orEmpty()).filter(::isMovable).map { it.key })
    }
    LaunchedEffect(editing.active) { if (!editing.active) reorder.reset() }
    LaunchedEffect(dragging) {
        while (dragging) {
            withFrameNanos { }
            val overflow = reorder.overflow()
            if (overflow != 0f) {
                list.scrollBy((overflow * AUTO_SCROLL_FRACTION).coerceIn(-MAX_STEP_PX, MAX_STEP_PX))
                if (reorder.dragBy(0f)) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
            }
        }
    }
}

/** Share of the overflow scrolled per frame: faster the further past the edge. */
private const val AUTO_SCROLL_FRACTION = 0.2f
private const val MAX_STEP_PX = 40f
