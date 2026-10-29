package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.notifications.ConversationKey
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Test

class ShortcutUnreadTest {
    private val phalanx = PackageUser("org.phalanx.android", 0)
    private val app = SearchHit.App(
        DrawerEntry(LaunchableApp(AppKey(phalanx.packageName, "Main", 0), "Phalanx", true), AppBadge.None),
    )

    private fun room(id: String, serial: Long = 0) =
        SearchHit.Shortcut(AppShortcut(phalanx.packageName, id, serial, "Room $id"), owner = null)

    private fun badges(hits: List<SearchHit>) = hits.map { (it as? SearchHit.Shortcut)?.badge }

    @Test
    fun `conversation shortcuts get the unread count of their notifications`() {
        val unread = mapOf(ConversationKey(phalanx, "!a") to 3)
        val hits = listOf(app, room("!a"), room("!b"), room("!a", serial = 10)).withUnread(unread, showCounts = true)
        assertEquals(listOf(null, AppBadge.Count(3), AppBadge.None, AppBadge.None), badges(hits))
    }

    @Test
    fun `counts become dots when the user prefers dots`() {
        val hits = listOf(room("!a")).withUnread(mapOf(ConversationKey(phalanx, "!a") to 2), showCounts = false)
        assertEquals(listOf(AppBadge.Dot), badges(hits))
    }
}
