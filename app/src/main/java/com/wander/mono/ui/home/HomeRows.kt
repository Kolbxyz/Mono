package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.media.MediaState
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.ui.components.appBadge

/**
 * Builds the home list: the favourites in the user's order (keys of apps not installed right now
 * are skipped, not shown broken), the now-playing row right under the favourite that owns the
 * session, or first when that app is not a favourite, and the access prompt last when needed.
 */
internal fun assembleHomeRows(
    favorites: List<AppKey>,
    catalog: List<LaunchableApp>,
    badges: Map<PackageUser, Int>,
    media: MediaState,
    showCounts: Boolean,
): List<HomeRow> {
    val installed = catalog.associateBy { it.key }
    val rows: MutableList<HomeRow> = favorites.distinct().mapNotNull(installed::get).map { app ->
        val owner = PackageUser(app.key.packageName, app.key.userSerial)
        HomeRow.App(app, appBadge(badges[owner] ?: 0, showCounts))
    }.toMutableList()

    when (media) {
        is MediaState.Active -> {
            // Sessions are only readable for Mono's own profile, so the owner is a main-profile app.
            val ownerIndex = rows.indexOfFirst {
                it is HomeRow.App && it.app.isMainProfile && it.app.key.packageName == media.media.packageName
            }
            rows.add(ownerIndex + 1, HomeRow.NowPlaying(media.media))
        }
        MediaState.NoAccess -> rows.add(HomeRow.ListenerPrompt)
        MediaState.Idle -> Unit
    }
    return rows
}
