package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.notifications.ConversationKey
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.core.siblings.PhalanxUnread
import com.wander.mono.core.siblings.RoomUnread
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomUnreadTest {
    private val pkg = "org.phalanx.android"

    private fun room(id: String, serial: Long = 0, owner: String = pkg) =
        SearchHit.Shortcut(AppShortcut(owner, id, serial, "Room $id"), owner = null)

    private fun badges(hits: List<SearchHit>) = hits.map { (it as SearchHit.Shortcut).badge }

    private val phalanx = PhalanxUnread(
        pkg,
        userSerial = 0,
        rooms = mapOf("!a" to RoomUnread("!a", 4, false), "!m" to RoomUnread("!m", 0, true)),
    )

    @Test
    fun `phalanx counts replace the notification counts`() {
        val user = PackageUser(pkg, 0)
        val notified = mapOf(ConversationKey(user, "!a") to 1, ConversationKey(user, "!b") to 2)
        val hits = listOf(room("!a"), room("!b"))
            .withUnread(notified, showCounts = true)
            .withRoomUnread(phalanx, showCounts = true)
        assertEquals(listOf(AppBadge.Count(4), AppBadge.None), badges(hits))
    }

    @Test
    fun `a room marked unread without a count gets a dot, and dots win when counts are off`() {
        assertEquals(
            listOf(AppBadge.Dot, AppBadge.Dot),
            badges(listOf(room("!m"), room("!a")).withRoomUnread(phalanx, showCounts = false)),
        )
    }

    @Test
    fun `other apps, other profiles and an absent phalanx keep their notification badge`() {
        val other = room("!a", owner = "org.signal").copy(badge = AppBadge.Count(2))
        val work = room("!a", serial = 10).copy(badge = AppBadge.Count(1))
        val hits = listOf(other, work)
        val kept = hits.withRoomUnread(phalanx, showCounts = true)
        assertEquals(listOf(AppBadge.Count(2), AppBadge.Count(1)), badges(kept))
        assertEquals(hits, hits.withRoomUnread(null, showCounts = true))
    }
}
