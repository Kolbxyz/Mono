package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.calendar.CalendarAgenda
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GlanceEventTest {
    private val lunch = CalendarEvent(1, "Lunch", begin = 60_000, end = 120_000, allDay = false, declined = false)
    private val agenda = CalendarAgenda(listOf(lunch), at = 0, appPackage = "at.bitfire.etar")
    private val signal = LaunchableApp(AppKey("org.signal", "org.signal.Main", 0), "Signal", true)
    private val favourite = HomeRow.App(signal, AppBadge.None)

    @Test
    fun `the header shows the next event when home has no calendar row`() {
        assertEquals(lunch, glanceEvent(agenda, now = 0, rows = listOf(favourite)))
    }

    @Test
    fun `the header leaves the event to the calendar row when it shows`() {
        val rows = listOf(favourite, CalendarRow(listOf(lunch), at = 0))
        assertNull(glanceEvent(agenda, now = 0, rows = rows))
    }

    @Test
    fun `nothing while rows load or without an agenda`() {
        assertNull(glanceEvent(agenda, now = 0, rows = null))
        assertNull(glanceEvent(null, now = 0, rows = listOf(favourite)))
    }
}
