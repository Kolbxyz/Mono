package com.wander.mono.ui.edit

import androidx.compose.runtime.Stable
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.widgets.WidgetSize
import com.wander.mono.data.LiveRowRef
import com.wander.mono.data.moved
import com.wander.mono.ui.home.HomeRow

/**
 * Edit mode as home sees it: whether it is on, the live row switches per favourite, the running
 * drag and what edits do. Outside edit mode only [reorder] matters: holding a favourite and
 * dragging it starts edit mode with that drag.
 */
@Stable
class HomeEditing(
    val active: Boolean,
    val toggles: Map<AppKey, List<LiveRowToggle>>,
    val reorder: ReorderState,
    val onEnter: () -> Unit,
    val onArrange: (HomeArrangement) -> Unit,
    val onMove: (AppKey, Int) -> Unit,
    val onRemove: (AppKey) -> Unit,
    val onRename: (AppKey) -> Unit,
    val onSetLiveRow: (LiveRowRef, Boolean) -> Unit,
    val onResizeWidget: (appWidgetId: Int, WidgetSize) -> Unit,
    val onRemoveWidget: (appWidgetId: Int) -> Unit,
) {
    /**
     * The rows home shows: all of them normally; in edit mode, or while a drag settles, only the
     * favourites and widgets (live rows give way to their placeholders under each favourite), in
     * the dragged order.
     */
    fun shownRows(rows: List<HomeRow>?): List<HomeRow> {
        val all = rows.orEmpty()
        val order = reorder.order
        if (!active && order == null) return all
        val movable = all.filter(::isMovable)
        val byKey = movable.associateBy { it.key }
        return order?.let { dragged -> dragged.mapNotNull(byKey::get) + movable.filter { it.key !in dragged } }
            ?: movable
    }

    /** Ends the drag and saves the new order. */
    fun drop(rows: List<HomeRow>) {
        val order = reorder.drop() ?: return
        arrange(order, rows)
    }

    /** Moves the row keyed [key] [by] places among [siblings] (TalkBack's move actions) and saves it. */
    fun moveBy(key: String, by: Int, siblings: List<String>, rows: List<HomeRow>) =
        arrange(siblings.moved(key, by), rows)

    private fun arrange(order: List<Any>, rows: List<HomeRow>) {
        val byKey = rows.filter(::isMovable).associateBy { it.key }
        onArrange(homeArrangement(order.mapNotNull(byKey::get)))
    }
}
