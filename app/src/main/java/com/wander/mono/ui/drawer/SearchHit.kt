package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.contacts.Contact
import com.wander.mono.core.system.SettingsPage
import com.wander.mono.ui.components.AppBadge

/** One search result: an app (hidden ones included), a shortcut, a sum, a contact, a settings page or the web. */
sealed interface SearchHit {
    val key: String
    val label: String

    data class App(val entry: DrawerEntry) : SearchHit {
        override val key: String get() = "app:${entry.app.key.encode()}"
        override val label: String get() = entry.app.label
    }

    /**
     * [owner] is the app that published it, for its icon and name; null when it has no launcher entry.
     * [badge] marks a conversation shortcut with unread messages.
     */
    data class Shortcut(
        val shortcut: AppShortcut,
        val owner: LaunchableApp?,
        val badge: AppBadge = AppBadge.None,
    ) : SearchHit {
        override val key: String get() = "shortcut:${shortcut.userSerial}/${shortcut.packageName}/${shortcut.id}"
        override val label: String get() = shortcut.label
    }

    /** The query read as arithmetic; tapping copies [result]. */
    data class Calculation(val result: String) : SearchHit {
        override val key: String get() = "calculation"
        override val label: String get() = result
    }

    data class Person(val contact: Contact) : SearchHit {
        override val key: String get() = "contact:${contact.id}"
        override val label: String get() = contact.name
    }

    data class Setting(val page: SettingsPage) : SearchHit {
        override val key: String get() = "setting:${page.id}"
        override val label: String get() = page.label
    }

    /** Hands [query] to the browser; [appLabel] names it, null when Android would ask which one. */
    data class Web(val query: String, val appLabel: String?) : SearchHit {
        override val key: String get() = "web"
        override val label: String get() = query
    }
}
