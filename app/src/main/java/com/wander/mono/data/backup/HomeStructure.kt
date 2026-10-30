package com.wander.mono.data.backup

import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.HomeFolder
import com.wander.mono.data.Routine
import com.wander.mono.data.SwipeAction

/**
 * How the user shaped home beyond its order, as a backup carries it: folders, each favourite's
 * swipe left, apps whose notifications home leaves out, daily limits and routines. Empty in a
 * backup made before these existed.
 */
data class HomeStructure(
    val folders: List<HomeFolder> = emptyList(),
    val swipes: Map<AppKey, SwipeAction> = emptyMap(),
    val mutedApps: Set<AppKey> = emptySet(),
    val dailyLimits: Map<AppKey, Int> = emptyMap(),
    val routines: List<Routine> = emptyList(),
) {
    /** Every app key it names. */
    val keys: List<AppKey>
        get() = folders.flatMap { it.apps } + swipes.keys + mutedApps + dailyLimits.keys +
            routines.flatMap { it.apps }

    /** The same structure with each key moved by [move]; keys it maps to null are dropped, and folders left empty. */
    fun remapped(move: (AppKey) -> AppKey?): HomeStructure = HomeStructure(
        folders = folders.mapNotNull { folder ->
            folder.copy(apps = folder.apps.mapNotNull(move).distinct()).takeIf { it.apps.isNotEmpty() }
        },
        swipes = swipes.entries.mapNotNull { (key, action) -> move(key)?.let { it to action } }.toMap(),
        mutedApps = mutedApps.mapNotNullTo(mutableSetOf(), move),
        dailyLimits = dailyLimits.entries.mapNotNull { (key, minutes) -> move(key)?.let { it to minutes } }.toMap(),
        routines = routines.map { routine -> routine.copy(apps = routine.apps.mapNotNullTo(mutableSetOf(), move)) },
    )
}
