package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.contacts.Contact
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.PreviewConversation
import com.wander.mono.core.siblings.WandaTrack
import com.wander.mono.core.system.SettingsPage
import com.wander.mono.ui.components.AppBadge
import java.util.Locale

/**
 * One search result: an app (hidden ones included), a shortcut, a sum or conversion, a timer or
 * alarm, a link or text to hand off, a contact, a notification, a song, a settings page or the web.
 */
sealed interface SearchHit {
    val key: String
    val label: String

    /** The characters of [label] the query matched, highlighted in its row; empty for none. */
    val matched: List<IntRange> get() = emptyList()

    data class App(val entry: DrawerEntry, override val matched: List<IntRange> = emptyList()) : SearchHit {
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
        override val matched: List<IntRange> = emptyList(),
    ) : SearchHit {
        override val key: String get() = "shortcut:${shortcut.userSerial}/${shortcut.packageName}/${shortcut.id}"
        override val label: String get() = shortcut.label
    }

    /** The query read as arithmetic or a unit conversion ("3.10686 mi"); tapping copies [result]. */
    data class Calculation(val result: String) : SearchHit {
        override val key: String get() = "calculation"
        override val label: String get() = result
    }

    /** A timer of [seconds] for the clock app; its row formats the length for the locale. */
    data class Timer(val seconds: Int) : SearchHit {
        override val key: String get() = "timer"
        override val label: String get() = "${seconds}s"
    }

    /** An alarm for the clock app; its row formats the time for the locale and 12/24-hour setting. */
    data class Alarm(val time: AlarmTime) : SearchHit {
        override val key: String get() = "alarm"
        override val label: String get() = "%02d:%02d".format(Locale.ROOT, time.hour, time.minute)
    }

    /** The query read as a web address; tapping opens it in the app that takes it. */
    data class Link(val url: String) : SearchHit {
        override val key: String get() = "link"
        override val label: String get() = url
    }

    /** Pasted text (or a link) handed to another app through Android's share sheet. */
    data class ShareText(val text: String) : SearchHit {
        override val key: String get() = "share"
        override val label: String get() = text
    }

    /** The contact's phone and email decide which of its Call, Message and Email actions show. */
    data class Person(val contact: Contact, override val matched: List<IntRange> = emptyList()) : SearchHit {
        override val key: String get() = "contact:${contact.id}"
        override val label: String get() = contact.name
    }

    /**
     * A song in Wanda's library; tapping hands it to Wanda to play. [owner] is the Wanda build that
     * found it, for its icon; null when it has no launcher entry (or is hidden).
     */
    data class Track(val track: WandaTrack, val owner: LaunchableApp? = null) : SearchHit {
        override val key: String get() = "track:${track.packageName}/${track.trackId}"
        override val label: String get() = track.title
    }

    /**
     * A posted notification of [app] whose title or text matched, as much of it as its redaction
     * lets show: one [conversation] of the app's expanded preview, or the [preview] itself when it
     * has none. Tapping fires its own tap intent. [matched] is in [title], [textMatched] in [text].
     */
    data class Notice(
        val preview: NotificationPreview,
        val conversation: PreviewConversation?,
        val app: LaunchableApp,
        val title: String,
        val text: String?,
        override val matched: List<IntRange> = emptyList(),
        val textMatched: List<IntRange> = emptyList(),
    ) : SearchHit {
        override val key: String get() = "notice:${conversation?.key ?: preview.key}"
        override val label: String get() = title
    }

    data class Setting(val page: SettingsPage, override val matched: List<IntRange> = emptyList()) : SearchHit {
        override val key: String get() = "setting:${page.id}"
        override val label: String get() = page.label
    }

    /** Hands [query] to the browser; [appLabel] names it, null when Android would ask which one. */
    data class Web(val query: String, val appLabel: String?) : SearchHit {
        override val key: String get() = "web"
        override val label: String get() = query
    }
}
