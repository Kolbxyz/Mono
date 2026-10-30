package com.wander.mono.ui.edit

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.wander.mono.R
import com.wander.mono.ui.components.rememberMonoHaptics
import com.wander.mono.ui.home.HomeRow
import com.wander.mono.ui.home.WidgetHomeRow

/**
 * A widget in edit mode: dragged among the favourites and other widgets like a favourite is
 * (hold anywhere on its card, or touch the handle), with the same lift, haptics and auto-scroll.
 * [siblings] are the movable rows' keys in shown order.
 */
@Composable
fun ReorderableWidget(
    row: WidgetHomeRow,
    siblings: List<String>,
    rows: List<HomeRow>,
    editing: HomeEditing,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberMonoHaptics()
    val interactions = remember { MutableInteractionSource() }
    val reorder = editing.reorder
    val lifted = reorder.dragging == row.key
    val settle = rememberDropSettle()
    val settleSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val appWidgetId = row.widget.placement.appWidgetId
    val callbacks = rememberUpdatedState(
        FavoriteGestureCallbacks(
            editing = true,
            onTap = {},
            onSwipeRight = null,
            onHeld = { haptics.longPress() },
            onHold = {},
            onLift = { reorder.start(row.key, siblings) },
            onDrag = { dy -> if (reorder.dragBy(dy)) haptics.segment() },
            onDrop = {
                if (reorder.dragging == row.key) {
                    haptics.gestureEnd()
                    settle.release(reorder.displacement, settleSpec)
                    editing.drop(rows)
                }
            },
        ),
    )
    Box(
        Modifier
            .reorderLift(lifted, reorder, settle)
            .then(modifier)
            .moveSemantics(row.key, siblings, rows, editing)
            .pointerInput(row.key) { detectFavoriteGestures(interactions, callbacks) },
    ) {
        EditWidgetRow(
            row = row,
            lifted = lifted,
            onResize = { editing.onResizeWidget(appWidgetId, it) },
            onRemove = { editing.onRemoveWidget(appWidgetId) },
            handle = Modifier.pointerInput(row.key) { detectHandleDrag(callbacks) },
        )
    }
}

/** The moves a drag does, for TalkBack: to the top, up and down among [siblings]. */
@Composable
internal fun Modifier.moveSemantics(
    key: String,
    siblings: List<String>,
    rows: List<HomeRow>,
    editing: HomeEditing,
): Modifier {
    val moveUp = stringResource(R.string.sheet_move_up)
    val moveDown = stringResource(R.string.sheet_move_down)
    val moveToTop = stringResource(R.string.edit_move_to_top)
    val index = siblings.indexOf(key)
    return semantics {
        customActions = buildList {
            if (index > 0) {
                add(CustomAccessibilityAction(moveToTop) { editing.moveBy(key, -index, siblings, rows).let { true } })
                add(CustomAccessibilityAction(moveUp) { editing.moveBy(key, -1, siblings, rows).let { true } })
            }
            if (index in 0 until siblings.lastIndex) {
                add(CustomAccessibilityAction(moveDown) { editing.moveBy(key, 1, siblings, rows).let { true } })
            }
        }
    }
}
