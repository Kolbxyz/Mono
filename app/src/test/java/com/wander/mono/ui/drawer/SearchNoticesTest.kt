package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.core.notifications.PreviewConversation
import com.wander.mono.core.notifications.PreviewExpansion
import com.wander.mono.core.notifications.PreviewMessage
import com.wander.mono.core.notifications.Redaction
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchNoticesTest {
    private fun entry(label: String, pkg: String) =
        DrawerEntry(LaunchableApp(AppKey(pkg, "$pkg.Main", 0), label, isMainProfile = true), AppBadge.None)

    private val signal = entry("Signal", "org.signal")
    private val mail = entry("Mail", "org.mail")

    private fun preview(
        pkg: String,
        title: String?,
        text: String?,
        redaction: Redaction = Redaction.Full,
        conversations: List<PreviewConversation> = emptyList(),
    ) = NotificationPreview(
        key = "$pkg|$title",
        owner = PackageUser(pkg, 0),
        title = title,
        text = text,
        redaction = redaction,
        contentIntent = null,
        autoCancel = true,
        isClearable = true,
        reply = null,
        expansion = PreviewExpansion(conversations, emptyList(), emptyList()),
    )

    private fun conversation(key: String, title: String?, vararg messages: PreviewMessage) =
        PreviewConversation(
            key, title, messages.toList(), Redaction.Full, contentIntent = null, autoCancel = true,
            owner = PackageUser("app", 0),
        )

    private fun find(query: String, vararg previews: NotificationPreview) =
        SearchNotices.find(query, previews.toList(), listOf(signal, mail))

    @Test
    fun `title or text match, with what matched marked in each`() {
        val hit = find("invoice", preview("org.mail", "Invoice 42", "Your invoice is ready")).single()
        assertEquals("Invoice 42", hit.title)
        assertEquals(listOf(0..6), hit.matched)
        assertEquals(listOf(5..11), hit.textMatched)
        assertEquals(mail.app, hit.app)
        assertNull(hit.conversation)
    }

    @Test
    fun `hidden content is never searched`() {
        assertTrue(find("secret", preview("org.mail", "Secret", "secret", Redaction.Hidden)).isEmpty())
    }

    @Test
    fun `apps search leaves out leave their notifications out`() {
        assertTrue(find("hello", preview("org.hidden", "Hello", "hello there")).isEmpty())
    }

    @Test
    fun `without a title of its own a notification is titled but never found by its app's name`() {
        val untitled = preview("org.signal", null, "Dinner at eight")
        assertTrue(find("signal", untitled).isEmpty())
        assertEquals("Signal", find("dinner", untitled).single().title)
    }

    @Test
    fun `each conversation of an expanded preview is its own result`() {
        val chats = preview(
            "org.signal",
            "Climbing",
            "Léa: on my way",
            conversations = listOf(
                conversation("c1", "Climbing", PreviewMessage("Léa", "On my way")),
                conversation("c2", "Family", PreviewMessage("Mum", "Lunch on sunday?")),
            ),
        )
        val hit = find("lunch", chats).single()
        assertEquals("c2", hit.conversation?.key)
        assertEquals("notice:c2", hit.key)
        assertEquals("Mum: Lunch on sunday?", hit.text)
        assertEquals(listOf(5..9), hit.textMatched)
    }

    @Test
    fun `best matches come first, capped`() {
        val previews = (1..6).map { preview("org.mail", "Report $it", "about the report") } +
            preview("org.signal", "Report", "exact")
        val hits = SearchNotices.find("report", previews, listOf(signal, mail))
        assertEquals(SearchNotices.MAX_NOTICES, hits.size)
        assertEquals("Report", hits.first().title)
    }

    @Test
    fun `typos are forgiven in notifications too`() {
        assertEquals(1, find("invoyce", preview("org.mail", "Invoice 42", null)).size)
    }
}
