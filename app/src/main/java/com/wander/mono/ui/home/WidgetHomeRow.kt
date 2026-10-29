package com.wander.mono.ui.home

import com.wander.mono.core.widgets.PlacedWidget
import com.wander.mono.core.widgets.layOutWidgets

/** A hosted third-party widget, under the favourite it was placed after. */
data class WidgetHomeRow(val widget: PlacedWidget) : HomeRow {
    override val key: String get() = "widget:${widget.placement.appWidgetId}"
}

/**
 * Puts the widgets into the assembled home list: each one under its favourite, after that
 * favourite's now-playing row so the media stays right under its app; widgets placed above the
 * first favourite go first.
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
        if (row !is HomeRow.NowPlaying) flushOwner()
        result += row
        if (row is HomeRow.App) owner = row
    }
    flushOwner()
    return result
}
