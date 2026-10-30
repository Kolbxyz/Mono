package com.wander.mono.ui.home

import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.media.MediaState
import com.wander.mono.core.media.PlayingMedia

/** The player under the clock: what plays and the app playing it ([app] null when home cannot launch it). */
data class HomePlayer(val media: PlayingMedia, val app: LaunchableApp?)

/**
 * The player for [state] while it is [shown] (the user's switch), with its app from [catalog].
 * Sessions are only readable for Mono's own profile, so the app is the main-profile entry.
 */
internal fun homePlayer(state: MediaState, catalog: List<LaunchableApp>, shown: Boolean): HomePlayer? {
    val media = (state as? MediaState.Active)?.media?.takeIf { shown } ?: return null
    return HomePlayer(media, catalog.firstOrNull { it.isMainProfile && it.key.packageName == media.packageName })
}
