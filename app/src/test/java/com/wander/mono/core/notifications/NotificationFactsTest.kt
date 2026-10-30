package com.wander.mono.core.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationFactsTest {
    private val phalanx = PackageUser("org.phalanx.android", 0)
    private val workPhalanx = PackageUser("org.phalanx.android", 10)
    private val wanda = PackageUser("com.wander.android", 0)

    private fun fact(
        key: String,
        owner: PackageUser,
        time: Long = 0,
        ongoing: Boolean = false,
        summary: Boolean = false,
        silent: Boolean = false,
        shortcut: String? = null,
        unread: Int = 1,
    ) = NotificationFacts(key, owner, time, ongoing, summary, silent, shortcut, unread)

    @Test
    fun `each app previews its most recent non-silent news`() {
        val latest = latestPerApp(
            listOf(
                fact("a", phalanx, time = 10),
                fact("b", phalanx, time = 30),
                fact("c", phalanx, time = 40, silent = true),
                fact("d", phalanx, time = 50, summary = true),
                fact("e", wanda, time = 99, ongoing = true),
                fact("f", workPhalanx, time = 5),
            )
        )
        assertEquals(mapOf(phalanx to "b", workPhalanx to "f"), latest)
    }

    @Test
    fun `unread adds up per conversation shortcut and profile`() {
        val unread = unreadPerConversation(
            listOf(
                fact("a", phalanx, shortcut = "!room1", unread = 3),
                fact("b", phalanx, shortcut = "!room1", unread = 2),
                fact("c", phalanx, shortcut = "!room2"),
                fact("d", phalanx, shortcut = "!room2", summary = true, unread = 9),
                fact("e", phalanx),
                fact("f", workPhalanx, shortcut = "!room1", unread = 0),
            )
        )
        assertEquals(
            mapOf(
                ConversationKey(phalanx, "!room1") to 5,
                ConversationKey(phalanx, "!room2") to 1,
                ConversationKey(workPhalanx, "!room1") to 1,
            ),
            unread,
        )
    }

    @Test
    fun `silent conversations still count as unread`() {
        val unread = unreadPerConversation(listOf(fact("a", phalanx, silent = true, shortcut = "!quiet")))
        assertEquals(mapOf(ConversationKey(phalanx, "!quiet") to 1), unread)
    }

    @Test
    fun `news of an app leaves out its status and other apps`() {
        val facts = listOf(
            fact("a", phalanx),
            fact("b", phalanx, ongoing = true),
            fact("c", phalanx, summary = true),
            fact("d", phalanx, silent = true),
            fact("e", workPhalanx),
        )
        assertEquals(listOf("a", "d"), newsOf(facts, phalanx).map { it.key })
    }

    @Test
    fun `conversations group by shortcut, newest first, latest notification each`() {
        val keys = conversationKeys(
            listOf(
                fact("a", phalanx, time = 10, shortcut = "!room1"),
                fact("b", phalanx, time = 40, shortcut = "!room1"),
                fact("c", phalanx, time = 30, shortcut = "!room2"),
                fact("d", phalanx, time = 20),
                fact("e", phalanx, time = 5),
            ),
            limit = 3,
        )
        assertEquals(listOf("b", "c", "d"), keys)
    }
}
