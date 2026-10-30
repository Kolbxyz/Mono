package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.ui.components.appBadge

/**
 * Builds the home list: the favourites in the user's order (keys of apps not installed right now
 * are skipped, not shown broken), and the access prompt last while notification access is
 * missing ([listenerMissing]).
 */
internal fun assembleHomeRows(
    favorites: List<AppKey>,
    catalog: List<LaunchableApp>,
    badges: Map<PackageUser, Int>,
    listenerMissing: Boolean,
    showCounts: Boolean,
): List<HomeRow> {
    val installed = catalog.associateBy { it.key }
    val rows = favorites.distinct().mapNotNull(installed::get).map { app ->
        val owner = PackageUser(app.key.packageName, app.key.userSerial)
        HomeRow.App(app, appBadge(badges[owner] ?: 0, showCounts))
    }
    return if (listenerMissing) rows + HomeRow.ListenerPrompt else rows
}
