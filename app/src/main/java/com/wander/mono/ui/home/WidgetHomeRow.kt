package com.wander.mono.ui.home

import com.wander.mono.core.widgets.PlacedWidget
import com.wander.mono.core.widgets.layOutWidgets

/** A hosted third-party widget, under the favourite it was placed after. */
data class WidgetHomeRow(val widget: PlacedWidget) : HomeRow {
    override val key: String get() = "widget:${widget.placement.appWidgetId}"
}

/**
 * Puts the widgets into the assembled home list: each one under its favourite, after that
 * favourite's own live rows (now playing, notification preview, calendar, live activities) so they stay right under
 * their app; widgets placed above the first favourite go first.
 */
internal fun withWidgetRows(rows: List<HomeRow>, widgets: List<PlacedWidget>): List<HomeRow> {
    if (widgets.isEmpty()) return rows
    val favorites = rows.filterIsInstance<HomeRow.App>().map { it.app.key }
    val layout = layOutWidgets(favorites, widgets) { it.placement.anchor }
    val result = layout.top.mapTo(mutableListOf<HomeRow>(), ::WidgetHomeRow)
    var owner: HomeRow.App? = null
    fun flushOwner() {
        owner?.let { app -> layout.under[app.app.key].orEmpty().mapTo(result, ::WidgetHomeRow) }
        owner = null
    }
    for (row in rows) {
        if (!row.belongsToAppAbove()) flushOwner()
        result += row
        if (row is HomeRow.App) owner = row
    }
    flushOwner()
    return result
}

/** Live rows that sit right under the favourite they belong to. */
private fun HomeRow.belongsToAppAbove(): Boolean =
    this is HomeRow.NowPlaying || this is NotificationRow || this is CalendarRow || this is LiveActivityRow
