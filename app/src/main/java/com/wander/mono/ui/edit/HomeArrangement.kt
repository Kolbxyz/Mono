package com.wander.mono.ui.edit

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.widgets.WidgetPlacement
import com.wander.mono.data.NowPlayingAnchor
import com.wander.mono.ui.home.HomeRow
import com.wander.mono.ui.home.WidgetHomeRow

/**
 * Home's movable rows, as edit mode leaves them: the favourites in order, each widget with the
 * favourite now right above it ([WidgetSpot.anchor], null above the first favourite) and, when it
 * was the one moved, the now-playing card's new place ([nowPlaying]; null leaves it as it was).
 * Favourites keep their ordered list and widgets their anchor, so a drag saves into the same
 * places the sheets already write.
 */
data class HomeArrangement(
    val favorites: List<AppKey>,
    val widgets: List<WidgetSpot>,
    val nowPlaying: NowPlayingAnchor? = null,
)

data class WidgetSpot(val appWidgetId: Int, val anchor: AppKey?)

/**
 * Reads the arrangement off [rows] shown top to bottom; live rows ride with their app and are
 * skipped. The now-playing spot is read only when [placedNowPlaying]: moving anything else must not
 * pin a card that follows its app.
 */
internal fun homeArrangement(rows: List<HomeRow>, placedNowPlaying: Boolean = false): HomeArrangement {
    var anchor: AppKey? = null
    val favorites = mutableListOf<AppKey>()
    val widgets = mutableListOf<WidgetSpot>()
    var nowPlaying: NowPlayingAnchor? = null
    rows.forEach { row ->
        when (row) {
            is HomeRow.App -> {
                anchor = row.app.key
                favorites += row.app.key
            }
            is WidgetHomeRow -> widgets += WidgetSpot(row.widget.placement.appWidgetId, anchor)
            HomeRow.NowPlayingSpot -> nowPlaying = anchor?.let { NowPlayingAnchor.Under(it) } ?: NowPlayingAnchor.Top
            else -> Unit
        }
    }
    return HomeArrangement(favorites, widgets, nowPlaying.takeIf { placedNowPlaying })
}

/**
 * The placements re-anchored and put in the order of [spots]; placements not among them (a widget
 * not on screen right now) keep their anchor and follow, so nothing is lost.
 */
internal fun List<WidgetPlacement>.arranged(spots: List<WidgetSpot>): List<WidgetPlacement> {
    val byId = associateBy { it.appWidgetId }
    val placed = spots.mapNotNull { spot -> byId[spot.appWidgetId]?.copy(anchor = spot.anchor) }
    val moved = spots.mapTo(HashSet()) { it.appWidgetId }
    return placed + filter { it.appWidgetId !in moved }
}

/** Whether [row] is one edit mode lets the user drag: a favourite, a widget or the now-playing spot. */
internal fun isMovable(row: HomeRow): Boolean =
    row is HomeRow.App || row is WidgetHomeRow || row is HomeRow.NowPlayingSpot
