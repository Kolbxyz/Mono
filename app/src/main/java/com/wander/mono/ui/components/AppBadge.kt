package com.wander.mono.ui.components

/** How an app's notifications are marked next to its name. */
sealed interface AppBadge {
    data object None : AppBadge

    /** The user chose dots over counts. */
    data object Dot : AppBadge

    data class Count(val count: Int) : AppBadge {
        /** What the pill shows: the count, capped at [MAX_SHOWN_COUNT] (drawn as "99+"). */
        val shown: Int get() = count.coerceAtMost(MAX_SHOWN_COUNT)
        val isCapped: Boolean get() = count > MAX_SHOWN_COUNT
    }
}

/** The badge for an app with [count] unread items. */
fun appBadge(count: Int, showCounts: Boolean): AppBadge = when {
    count <= 0 -> AppBadge.None
    showCounts -> AppBadge.Count(count)
    else -> AppBadge.Dot
}

/** Past two digits a pill stops being glanceable; the exact count is still announced. */
const val MAX_SHOWN_COUNT = 99
