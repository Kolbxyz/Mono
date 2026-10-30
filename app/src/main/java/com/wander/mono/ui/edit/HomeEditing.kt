package com.wander.mono.ui.edit

import androidx.compose.runtime.Stable
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.widgets.WidgetSize
import com.wander.mono.data.LiveRowRef
import com.wander.mono.data.NowPlayingAnchor
import com.wander.mono.data.moved
import com.wander.mono.ui.home.HomeRow

/**
 * Edit mode as home sees it: whether it is on, the live row switches per favourite, where the
 * now-playing card was placed ([nowPlaying], null while it follows its app), the running drag and
 * what edits do. Outside edit mode only [reorder] matters: holding a favourite and dragging it
 * starts edit mode with that drag.
 */
@Stable
class HomeEditing(
    val active: Boolean,
    val toggles: Map<AppKey, List<LiveRowToggle>>,
    val nowPlaying: NowPlayingAnchor?,
    val reorder: ReorderState,
    val onEnter: () -> Unit,
    val onArrange: (HomeArrangement) -> Unit,
    val onMove: (AppKey, Int) -> Unit,
    val onRemove: (AppKey) -> Unit,
    val onRename: (AppKey) -> Unit,
    val onSetLiveRow: (LiveRowRef, Boolean) -> Unit,
    val onResizeWidget: (appWidgetId: Int, WidgetSize) -> Unit,
    val onRemoveWidget: (appWidgetId: Int) -> Unit,
    /** The now-playing card goes back under the app that plays. */
    val onUnplaceNowPlaying: () -> Unit,
) {
    /** In edit mode or while a drag settles, home shows the saved order, never one arranged for the moment. */
    val showsSavedOrder: Boolean get() = active || reorder.order != null

    /**
     * The rows home shows: all of them normally; in edit mode, or while a drag settles, only the
     * favourites, widgets and the now-playing spot (other live rows give way to their placeholders
     * under each favourite), in the dragged order.
     */
    fun shownRows(rows: List<HomeRow>?): List<HomeRow> {
        val all = rows.orEmpty()
        val order = reorder.order
        if (!showsSavedOrder) return all
        val movable = movableRows(all)
        val byKey = movable.associateBy { it.key }
        return order?.let { dragged -> dragged.mapNotNull(byKey::get) + movable.filter { it.key !in dragged } }
            ?: movable
    }

    /**
     * Drops the dragged order once [rows] show it saved. A drag begun outside edit mode, before the
     * now-playing spot showed, has no spot in its order, so the spot is left out of the comparison.
     */
    fun settle(rows: List<HomeRow>) {
        val keys = movableRows(rows).map { it.key }
        val spot = HomeRow.NowPlayingSpot.key
        reorder.settle(if (reorder.order?.contains(spot) == true) keys else keys - spot)
    }

    /** Ends the drag and saves the new order. */
    fun drop(rows: List<HomeRow>) {
        val dragged = reorder.dragging
        val order = reorder.drop() ?: return
        arrange(order, rows, dragged)
    }

    /** Moves the row keyed [key] [by] places among [siblings] (TalkBack's move actions) and saves it. */
    fun moveBy(key: String, by: Int, siblings: List<String>, rows: List<HomeRow>) =
        arrange(siblings.moved(key, by), rows, key)

    private fun movableRows(rows: List<HomeRow>): List<HomeRow> =
        withNowPlayingSpot(rows, nowPlaying).filter(::isMovable)

    /** Saves [order]; the now-playing card's place only when it is the row [moved]. */
    private fun arrange(order: List<Any>, rows: List<HomeRow>, moved: Any?) {
        val byKey = movableRows(rows).associateBy { it.key }
        val placed = moved == HomeRow.NowPlayingSpot.key
        onArrange(homeArrangement(order.mapNotNull(byKey::get), placedNowPlaying = placed))
    }
}
