package com.wander.mono.core.widgets

import com.wander.mono.core.apps.AppKey

/**
 * Where widgets go among the favourites on home: [top] above the first favourite, and [under] each
 * favourite in their stored order. A widget whose favourite is no longer on home moves under the
 * last favourite (or to the top when there is none), so it is never lost.
 */
internal class WidgetLayout<T>(val top: List<T>, val under: Map<AppKey, List<T>>)

internal fun <T> layOutWidgets(favorites: List<AppKey>, widgets: List<T>, anchorOf: (T) -> AppKey?): WidgetLayout<T> {
    val onHome = favorites.toSet()
    val fallback = favorites.lastOrNull()
    val resolved = widgets.groupBy { widget ->
        val anchor = anchorOf(widget)
        if (anchor == null || anchor in onHome) anchor else fallback
    }
    return WidgetLayout(
        top = resolved[null].orEmpty(),
        under = resolved.filterKeys { it != null }.mapKeys { (key, _) -> key!! },
    )
}

/** One step of the home order, as far as moving a widget is concerned. */
internal sealed interface HomeSlot {
    data class Favorite(val key: AppKey) : HomeSlot

    data class Widget(val placement: WidgetPlacement) : HomeSlot
}

/** Home's favourites and widgets as one sequence, top to bottom. */
internal fun homeSlots(favorites: List<AppKey>, placements: List<WidgetPlacement>): List<HomeSlot> {
    val layout = layOutWidgets(favorites, placements) { it.anchor }
    return layout.top.map(HomeSlot::Widget) + favorites.flatMap { key ->
        listOf(HomeSlot.Favorite(key)) + layout.under[key].orEmpty().map(HomeSlot::Widget)
    }
}

/**
 * Moves widget [appWidgetId] one row up ([by] -1) or down (+1) past a favourite or another widget,
 * then re-anchors every widget to the favourite now above it. Unchanged when it is already at the end.
 */
internal fun moveWidget(
    favorites: List<AppKey>,
    placements: List<WidgetPlacement>,
    appWidgetId: Int,
    by: Int,
): List<WidgetPlacement> {
    val slots = homeSlots(favorites, placements).toMutableList()
    val from = slots.indexOfFirst { it is HomeSlot.Widget && it.placement.appWidgetId == appWidgetId }
    val to = from + by
    if (from < 0 || to !in slots.indices) return placements
    slots.add(to, slots.removeAt(from))
    var anchor: AppKey? = null
    return slots.mapNotNull { slot ->
        when (slot) {
            is HomeSlot.Favorite -> {
                anchor = slot.key
                null
            }
            is HomeSlot.Widget -> slot.placement.copy(anchor = anchor)
        }
    }
}

/**
 * Where a new widget goes: under its own app when that app is a favourite in the same profile,
 * else under the last favourite, else at the top.
 */
internal fun defaultAnchor(favorites: List<AppKey>, packageName: String, userSerial: Long): AppKey? =
    favorites.firstOrNull { it.packageName == packageName && it.userSerial == userSerial } ?: favorites.lastOrNull()
