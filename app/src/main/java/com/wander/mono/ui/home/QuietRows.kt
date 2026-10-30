package com.wander.mono.ui.home

import com.wander.mono.core.notifications.LiveActivityKind
import com.wander.mono.core.quiet.QuietState
import com.wander.mono.ui.components.AppBadge

/**
 * Home in quiet mode: only the quiet favourites, without badges, the playing media (the user
 * started it) and, under a kept favourite, its running timer or directions: useful, not
 * distracting. Every other live row (progress, previews, prompts, and any added later) is left out.
 */
internal fun quietHomeRows(rows: List<HomeRow>, quiet: QuietState): List<HomeRow> {
    if (!quiet.enabled) return rows
    val kept = rows.mapNotNullTo(HashSet()) { row -> (row as? HomeRow.App)?.app?.key?.takeIf { it in quiet.apps } }
    return rows.mapNotNull { row ->
        when (row) {
            is HomeRow.App -> row.takeIf { it.app.key in kept }?.copy(badge = AppBadge.None)
            is HomeRow.NowPlaying -> row
            is LiveActivityRow -> row.takeIf { it.app.key in kept && it.activity.kind in QUIET_LIVE_KINDS }
            else -> null
        }
    }
}

/** The live rows quiet mode keeps: they help with what the user is doing rather than pull them away. */
private val QUIET_LIVE_KINDS = setOf(LiveActivityKind.Timer, LiveActivityKind.Navigation)
