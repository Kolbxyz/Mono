package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut

/**
 * The hidden apps, which the drawer never lists, and whether search may find them: always without
 * the lock, and after the screen lock was confirmed with it. While locked, their shortcuts stay
 * out of search too, so a conversation does not give a hidden app away.
 */
data class HiddenApps(val keys: Set<AppKey>, val revealed: Boolean) {
    /** Hidden apps exist and search leaves them out until unlocked. */
    val locked: Boolean get() = !revealed && keys.isNotEmpty()

    fun searchableApps(entries: List<DrawerEntry>): List<DrawerEntry> =
        if (revealed) entries else entries.filter { it.app.key !in keys }

    fun searchableShortcuts(shortcuts: List<AppShortcut>): List<AppShortcut> {
        if (revealed) return shortcuts
        val owners = keys.map { it.packageName to it.userSerial }.toSet()
        return shortcuts.filter { (it.packageName to it.userSerial) !in owners }
    }
}
