package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.calendar.CalendarAgenda
import com.wander.mono.core.media.MediaState
import com.wander.mono.core.notifications.PackageUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MutedOnHomeTest {
    private val chat = AppKey("org.chat", "org.chat.Main", 0)
    private val workChat = AppKey("org.chat", "org.chat.Main", 10)

    @Test
    fun `a muted app loses its counts in its own profile only`() {
        val counts = mapOf(
            PackageUser("org.chat", 0) to 3,
            PackageUser("org.chat", 10) to 1,
            PackageUser("org.mail", 0) to 2,
        )
        assertEquals(
            mapOf(PackageUser("org.chat", 10) to 1, PackageUser("org.mail", 0) to 2),
            counts.unmuted(setOf(chat)),
        )
        assertEquals(counts, counts.unmuted(emptySet()))
    }

    @Test
    fun `the calendar row goes when the calendar app is muted`() {
        val agenda = CalendarAgenda(emptyList(), at = 0, appPackage = "org.chat")
        assertNull(agenda.unlessMuted(setOf(workChat)))
        assertEquals(agenda, agenda.unlessMuted(setOf(AppKey("org.mail", "org.mail.Main", 0))))
    }

    @Test
    fun `media states without a session stay as they are`() {
        assertEquals(MediaState.Idle, MediaState.Idle.unlessMuted(setOf(chat)))
        assertEquals(MediaState.NoAccess, MediaState.NoAccess.unlessMuted(setOf(chat)))
    }
}
