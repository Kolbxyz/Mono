package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchableApp

/** One search result: an app (hidden ones included) or one of an app's shortcuts. */
sealed interface SearchHit {
    val key: String
    val label: String

    data class App(val entry: DrawerEntry) : SearchHit {
        override val key: String get() = "app:${entry.app.key.encode()}"
        override val label: String get() = entry.app.label
    }

    /** [owner] is the app that published it, for its icon and name; null when it has no launcher entry. */
    data class Shortcut(val shortcut: AppShortcut, val owner: LaunchableApp?) : SearchHit {
        override val key: String get() = "shortcut:${shortcut.userSerial}/${shortcut.packageName}/${shortcut.id}"
        override val label: String get() = shortcut.label
    }
}
