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

/**
 * The badge beside a name whose notification already shows as text under it ([previewShown]): a lone
 * notification needs no "1" next to its own words, so the pill only counts from two.
 */
fun AppBadge.besidePreview(previewShown: Boolean): AppBadge =
    if (previewShown && this is AppBadge.Count && count < 2) AppBadge.None else this

/** Past two digits a pill stops being glanceable; the exact count is still announced. */
const val MAX_SHOWN_COUNT = 99
