package com.wander.mono.core.apps

/** What `LauncherApps` or a profile broadcast reported as changed. */
sealed interface CatalogChange {
    data class Packages(val packageNames: Set<String>) : CatalogChange

    data class Shortcuts(val packageName: String) : CatalogChange

    /** A profile was added, removed, paused, resumed, locked or unlocked: everything may have changed. */
    data object Profiles : CatalogChange
}
