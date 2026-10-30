package com.wander.mono.ui.edit

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.calendar.CalendarAgenda
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.core.notifications.PreviewExpansion
import com.wander.mono.core.notifications.Redaction
import com.wander.mono.data.LiveRowKind
import org.junit.Assert.assertEquals
import org.junit.Test

class LiveRowAvailabilityTest {
    private val signal = app("org.signal", "Signal")
    private val workSignal = app("org.signal", "Signal", serial = 10, main = false)
    private val etar = app("at.bitfire.etar", "Calendar")

    private fun app(pkg: String, label: String, serial: Long = 0, main: Boolean = true) =
        LaunchableApp(AppKey(pkg, "$pkg.Main", serial), label, main)

    private fun preview(owner: PackageUser) = NotificationPreview(
        key = owner.packageName,
        owner = owner,
        title = null,
        text = "Hi",
        redaction = Redaction.Full,
        contentIntent = null,
        autoCancel = true,
        isClearable = true,
        reply = null,
        expansion = PreviewExpansion(emptyList(), emptyList(), emptyList()),
    )

    private fun agenda(events: Int) = CalendarAgenda(
        (1..events).map { CalendarEvent(it.toLong(), "Event", 1_000, 2_000, allDay = false, declined = false) },
        at = 0,
        appPackage = "at.bitfire.etar",
    )

    @Test
    fun `a preview switch belongs to the favourite in the notification's profile`() {
        val owner = PackageUser("org.signal", 10)
        val available = previewAvailability(mapOf(owner to preview(owner)), listOf(signal, workSignal, etar))
        assertEquals(mapOf(workSignal.key to setOf(LiveRowKind.NotificationPreview)), available)
    }

    @Test
    fun `a calendar switch belongs to the calendar app, only when it has events`() {
        val available = calendarAvailability(agenda(2), listOf(signal, etar))
        assertEquals(mapOf(etar.key to setOf(LiveRowKind.Calendar)), available)
        assertEquals(emptyMap<AppKey, Set<LiveRowKind>>(), calendarAvailability(agenda(0), listOf(etar)))
        assertEquals(emptyMap<AppKey, Set<LiveRowKind>>(), calendarAvailability(null, listOf(etar)))
    }

    @Test
    fun `availability merges per app`() {
        val merged = mergeAvailability(
            mapOf(signal.key to setOf(LiveRowKind.NowPlaying)),
            mapOf(signal.key to setOf(LiveRowKind.NotificationPreview), etar.key to setOf(LiveRowKind.Calendar)),
        )
        assertEquals(
            mapOf(
                signal.key to setOf(LiveRowKind.NowPlaying, LiveRowKind.NotificationPreview),
                etar.key to setOf(LiveRowKind.Calendar),
            ),
            merged,
        )
    }
}
