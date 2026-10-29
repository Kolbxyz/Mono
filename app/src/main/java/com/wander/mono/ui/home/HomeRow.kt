package com.wander.mono.ui.home

import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.ui.components.AppBadge

/**
 * One row of the home list. Rows are not only apps: live content (the playing media today,
 * widgets later) is a row of its own, placed next to the app it belongs to.
 */
sealed interface HomeRow {
    /** Stable `LazyColumn` key. */
    val key: String

    data class App(val app: LaunchableApp, val badge: AppBadge) : HomeRow {
        override val key: String get() = "app:${app.key.encode()}"
    }

    data class NowPlaying(val media: PlayingMedia) : HomeRow {
        override val key: String get() = "now-playing"
    }

    /** Notification access is missing, so there are no dots and no now-playing row. */
    data object ListenerPrompt : HomeRow {
        override val key: String get() = "listener-prompt"
    }
}
