package com.wander.mono.ui.onboarding

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp

/** The apps the favourites step offers, in order, and the ones it ticks before the user does. */
data class FavoriteSuggestions(val apps: List<LaunchableApp>, val preselected: List<AppKey>)

/**
 * What the favourites step lists, from the alphabetical [apps]. With today's foreground time by
 * package ([todayMillis], null without usage access) the most used come first, the rest staying
 * A to Z, and the top few used for at least [MIN_USE_MS] start ticked. Without it the list stays
 * A to Z and nothing is ticked: Mono does not guess. Private space apps never go on home.
 */
fun favoriteSuggestions(apps: List<LaunchableApp>, todayMillis: Map<String, Long>?): FavoriteSuggestions {
    val offered = apps.filterNot { it.isPrivate }
    if (todayMillis == null) return FavoriteSuggestions(offered, emptyList())
    // Usage is only readable for Mono's own profile; a work app with the same package was not that use.
    fun usedMillis(app: LaunchableApp) = if (app.isMainProfile) todayMillis[app.key.packageName] ?: 0L else 0L
    // A stable sort: apps used equally long keep their alphabetical order.
    val byUse = offered.sortedByDescending(::usedMillis)
    val preselected = byUse
        .filter { usedMillis(it) >= MIN_USE_MS }
        // An app with two launcher entries counts its time once.
        .distinctBy { it.key.packageName }
        .take(MAX_PRESELECTED)
        .map { it.key }
    return FavoriteSuggestions(byUse, preselected)
}

/** How many favourites usage ticks at most: a calm list is a short one. */
internal const val MAX_PRESELECTED = 5

/** Less than a minute today is a glance, not an app the user lives in. */
internal const val MIN_USE_MS = 60_000L
