package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey

/**
 * Home with the [lifted] favourites first (headphones are connected, a routine is on): each one moves with
 * the live rows under it (now playing, preview, calendar, widgets), and the lifted ones keep the
 * user's order among themselves, as do the rest. A folder goes first when any of its favourites
 * does. Rows above the first favourite stay first and the access prompt stays last. Only the
 * shown order changes; the saved one is untouched.
 */
internal fun liftedFirst(rows: List<HomeRow>, lifted: Set<AppKey>): List<HomeRow> {
    if (lifted.isEmpty()) return rows
    val leading = mutableListOf<HomeRow>()
    val groups = mutableListOf<MutableList<HomeRow>>()
    val trailing = mutableListOf<HomeRow>()
    for (row in rows) {
        when {
            row is HomeRow.ListenerPrompt -> trailing += row
            row is HomeRow.App || row is HomeRow.Folder -> groups += mutableListOf<HomeRow>(row)
            groups.isEmpty() -> leading += row
            else -> groups.last() += row
        }
    }
    val (up, rest) = groups.partition { group ->
        when (val head = group.first()) {
            is HomeRow.Folder -> head.apps.any { it.app.key in lifted }
            else -> (head as HomeRow.App).app.key in lifted
        }
    }
    return leading + up.flatten() + rest.flatten() + trailing
}
