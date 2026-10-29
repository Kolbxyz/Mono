package com.wander.mono.core.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService.Ranking
import org.junit.Assert.assertEquals
import org.junit.Test

class RedactionTest {
    private val noOverride = Ranking.VISIBILITY_NO_OVERRIDE

    private fun redaction(
        visibility: Int,
        override: Int = noOverride,
        locked: Boolean = false,
        public: Boolean = false,
    ) = redactionFor(visibility, override, locked, public)

    @Test
    fun `secret notifications never show content`() {
        assertEquals(Redaction.Hidden, redaction(Notification.VISIBILITY_SECRET, public = true))
    }

    @Test
    fun `the user's per-channel override can only hide more`() {
        val secret = Notification.VISIBILITY_SECRET
        val public = Notification.VISIBILITY_PUBLIC
        assertEquals(Redaction.Hidden, redaction(public, override = secret))
        // Secret from the app stays secret even when the user's channel setting is public.
        assertEquals(Redaction.Hidden, redaction(secret, override = public))
    }

    @Test
    fun `private content shows on an unlocked home`() {
        assertEquals(Redaction.Full, redaction(Notification.VISIBILITY_PRIVATE))
    }

    @Test
    fun `private content of a locked profile falls back to the public version, or nothing`() {
        assertEquals(Redaction.Public, redaction(Notification.VISIBILITY_PRIVATE, locked = true, public = true))
        assertEquals(Redaction.Hidden, redaction(Notification.VISIBILITY_PRIVATE, locked = true))
    }

    @Test
    fun `public content shows even for a locked profile`() {
        assertEquals(Redaction.Full, redaction(Notification.VISIBILITY_PUBLIC, locked = true))
    }
}
