package com.wander.mono.ui.home

import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppBadge

/**
 * One row of the home list. Rows are not only apps: live content (previews, the calendar, live
 * activities, widgets) is a row of its own, placed next to the app it belongs to. The playing media
 * is not a row: it lives under the clock, in the glance header.
 */
sealed interface HomeRow {
    /** Stable `LazyColumn` key. */
    val key: String

    data class App(val app: LaunchableApp, val badge: AppBadge) : HomeRow {
        override val key: String get() = "app:${app.key.encode()}"
    }

    /**
     * Favourites the user grouped, where the first of them is in the saved order; tapping it shows
     * its [apps] under it. Its [badge] adds theirs up.
     */
    data class Folder(val id: String, val name: String, val apps: List<App>, val badge: AppBadge) : HomeRow {
        override val key: String get() = "folder:$id"
    }

    /** Notification access is missing, so there are no dots and no now-playing player. */
    data object ListenerPrompt : HomeRow {
        override val key: String get() = "listener-prompt"
    }
}
