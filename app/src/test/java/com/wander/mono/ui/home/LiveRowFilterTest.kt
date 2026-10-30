package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.calendar.CalendarAgenda
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.core.notifications.PreviewExpansion
import com.wander.mono.core.notifications.Redaction
import com.wander.mono.data.LiveRowKind
import com.wander.mono.data.LiveRowRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class LiveRowFilterTest {
    private val signal = AppKey("org.signal", "org.signal.Main", 0)
    private val workSignal = AppKey("org.signal", "org.signal.Main", 10)
    private val etar = AppKey("at.bitfire.etar", "at.bitfire.etar.Main", 0)

    private fun preview(owner: PackageUser) = NotificationPreview(
        key = "0|${owner.packageName}|${owner.userSerial}",
        owner = owner,
        title = "Léa",
        text = "Hi",
        redaction = Redaction.Full,
        contentIntent = null,
        autoCancel = true,
        isClearable = true,
        reply = null,
        expansion = PreviewExpansion(emptyList(), emptyList(), emptyList()),
    )

    private val previews = listOf(PackageUser("org.signal", 0), PackageUser("org.signal", 10))
        .associateWith(::preview)

    private val agenda = CalendarAgenda(
        listOf(CalendarEvent(1, "Lunch", 1_000, 2_000, allDay = false, declined = false)),
        at = 0,
        appPackage = "at.bitfire.etar",
    )

    @Test
    fun `a preview switched off hides only in that profile`() {
        val off = setOf(LiveRowRef(workSignal, LiveRowKind.NotificationPreview))
        assertEquals(setOf(PackageUser("org.signal", 0)), previews.unlessHidden(off).keys)
    }

    @Test
    fun `previews stay when nothing of their kind is off`() {
        assertSame(previews, previews.unlessHidden(setOf(LiveRowRef(signal, LiveRowKind.Calendar))))
    }

    @Test
    fun `the calendar row hides when switched off for the calendar app`() {
        assertNull(agenda.unlessHidden(setOf(LiveRowRef(etar, LiveRowKind.Calendar))))
        assertSame(agenda, agenda.unlessHidden(setOf(LiveRowRef(signal, LiveRowKind.Calendar))))
        assertSame(agenda, agenda.unlessHidden(setOf(LiveRowRef(etar, LiveRowKind.NotificationPreview))))
        assertNull((null as CalendarAgenda?).unlessHidden(emptySet()))
    }
}
