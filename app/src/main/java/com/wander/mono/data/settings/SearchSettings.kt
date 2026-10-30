package com.wander.mono.data.settings

/** What search looks in besides apps. A source switched off is not asked at all, not just hidden. */
object SearchSettings {
    /** Find contacts by name in search. Off by default: it needs `READ_CONTACTS`, asked for when switched on. */
    val Contacts = BooleanSetting("search_contacts", default = false)

    /** Apps' shortcuts (a chat, a new note) alongside the apps. */
    val Shortcuts = BooleanSetting("search_shortcuts", default = true)

    /** Pages of Android's settings app (Wi-Fi, Bluetooth) by name. */
    val SettingsPages = BooleanSetting("search_settings_pages", default = true)

    /** Sums, unit conversions, and timers and alarms handed to the clock app. */
    val QuickAnswers = BooleanSetting("search_quick_answers", default = true)

    /**
     * Titles and text of posted notifications, as much of them as their previews may show. Off by
     * default: it needs notification access, and a match brings a message's content into search.
     */
    val Notifications = BooleanSetting("search_notifications", default = false)

    /**
     * Remember the last searches that opened something, offered again while the field is empty. Off
     * by default; what was searched stays on the phone, out of backups ([com.wander.mono.data.SearchHistory]).
     */
    val RememberRecent = BooleanSetting("search_remember_recent", default = false)
}
