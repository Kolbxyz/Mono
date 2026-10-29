package com.wander.mono.ui.home

import com.wander.mono.core.quiet.QuietState
import com.wander.mono.ui.components.AppBadge

/**
 * Home in quiet mode: only the quiet favourites, without badges, and the playing media (the user
 * started it). Every other live row (previews, prompts, and any added later) is left out.
 */
internal fun quietHomeRows(rows: List<HomeRow>, quiet: QuietState): List<HomeRow> {
    if (!quiet.enabled) return rows
    return rows.mapNotNull { row ->
        when (row) {
            is HomeRow.App -> row.takeIf { it.app.key in quiet.apps }?.copy(badge = AppBadge.None)
            is HomeRow.NowPlaying -> row
            else -> null
        }
    }
}
