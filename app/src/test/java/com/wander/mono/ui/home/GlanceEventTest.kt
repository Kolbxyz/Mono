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

    @Test
    fun `an event under way shows until the next one is close`() {
        val minute = 60_000L
        val meeting = CalendarEvent(2, "Meeting", begin = 0, end = 60 * minute, allDay = false, declined = false)
        val later = CalendarEvent(3, "Later", begin = 45 * minute, end = 90 * minute, allDay = false, declined = false)
        val both = CalendarAgenda(listOf(meeting, later), at = 0, appPackage = null)
        val rows = listOf(favourite)
        assertEquals(meeting, glanceEvent(both, now = 10 * minute, rows = rows))
        assertEquals(later, glanceEvent(both, now = 30 * minute, rows = rows))
    }

    @Test
    fun `an all-day, declined or ended event is never the one under way`() {
        val allDay = CalendarEvent(4, "Holiday", begin = 0, end = 100, allDay = true, declined = false)
        val declined = CalendarEvent(5, "No", begin = 0, end = 100, allDay = false, declined = true)
        val ended = CalendarEvent(6, "Done", begin = 0, end = 50, allDay = false, declined = false)
        assertNull(currentEvent(listOf(allDay, declined, ended), now = 50))
    }
}
