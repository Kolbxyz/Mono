package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HiddenAppsTest {
    private val secret = entry("a.secret", "Secret")
    private val open = entry("b.open", "Open")
    private val secretChat = AppShortcut("a.secret", "chat", 0, "Chat")
    private val workSecretChat = AppShortcut("a.secret", "chat", 10, "Chat")
    private val entries = listOf(secret, open)

    private fun entry(pkg: String, label: String) =
        DrawerEntry(LaunchableApp(AppKey(pkg, "$pkg.Main", 0), label, true), AppBadge.None)

    @Test
    fun `revealed search finds hidden apps and their shortcuts`() {
        val hidden = HiddenApps(setOf(secret.app.key), revealed = true)
        assertEquals(entries, hidden.searchableApps(entries))
        assertEquals(listOf(secretChat), hidden.searchableShortcuts(listOf(secretChat)))
        assertFalse(hidden.locked)
    }

    @Test
    fun `locked search leaves out hidden apps and their shortcuts, per profile`() {
        val hidden = HiddenApps(setOf(secret.app.key), revealed = false)
        assertEquals(listOf(open), hidden.searchableApps(entries))
        assertEquals(listOf(workSecretChat), hidden.searchableShortcuts(listOf(secretChat, workSecretChat)))
        assertTrue(hidden.locked)
    }

    @Test
    fun `nothing hidden is never locked`() {
        assertFalse(HiddenApps(emptySet(), revealed = false).locked)
    }
}
