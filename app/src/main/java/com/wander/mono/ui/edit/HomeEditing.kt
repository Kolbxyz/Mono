package com.wander.mono.ui.edit

import androidx.compose.runtime.Stable
import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.LiveRowRef
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
    val onReorder: (List<AppKey>) -> Unit,
    val onMove: (AppKey, Int) -> Unit,
    val onRemove: (AppKey) -> Unit,
    val onRename: (AppKey) -> Unit,
    val onSetLiveRow: (LiveRowRef, Boolean) -> Unit,
) {
    /** In edit mode or while a drag settles, home shows the saved order, never one arranged for the moment. */
    val showsSavedOrder: Boolean get() = active || reorder.order != null

    /**
     * The rows home shows: all of them normally; in edit mode, or while a drag settles, only the
     * favourites (live rows give way to their switches), in the dragged order.
     */
    fun shownRows(rows: List<HomeRow>?): List<HomeRow> {
        val all = rows.orEmpty()
        val order = reorder.order
        if (!showsSavedOrder) return all
        val apps = all.filterIsInstance<HomeRow.App>()
        val byKey = apps.associateBy { it.key }
        return order?.let { dragged -> dragged.mapNotNull(byKey::get) + apps.filter { it.key !in dragged } } ?: apps
    }

    /** Ends the drag and saves the new order. */
    fun drop(rows: List<HomeRow>) {
        val order = reorder.drop() ?: return
        val byKey = rows.filterIsInstance<HomeRow.App>().associateBy { it.key }
        onReorder(order.mapNotNull { byKey[it]?.app?.key })
    }
}
