package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.calendar.CalendarAgenda
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class CalendarRowTest {
    private val etar = app("at.bitfire.etar", "Etar")
    private val workEtar = app("at.bitfire.etar", "Etar", serial = 10, main = false)
    private val clock = app("com.android.deskclock", "Clock")

    private fun app(pkg: String, label: String, serial: Long = 0, main: Boolean = true) =
        HomeRow.App(LaunchableApp(AppKey(pkg, "$pkg.Main", serial), label, main), AppBadge.None)

    private fun event(id: Long) = CalendarEvent(id, "Event $id", id * 1_000, id * 1_000 + 500, false, false)

    private fun agenda(count: Int, pkg: String? = "at.bitfire.etar") =
        CalendarAgenda((1..count).map { event(it.toLong()) }, at = 0, appPackage = pkg)

    private fun shape(rows: List<HomeRow>) = rows.map {
        when (it) {
            is HomeRow.App -> it.app.label + if (it.app.isMainProfile) "" else " (work)"
            is CalendarRow -> "calendar:${it.events.size}"
            else -> "other"
        }
    }

    @Test
    fun `row sits under the main-profile calendar app when it is a favourite`() {
        val rows = withCalendarRow(listOf(clock, workEtar, etar), agenda(2))
        assertEquals(listOf("Clock", "Etar (work)", "Etar", "calendar:2"), shape(rows))
    }

    @Test
    fun `row goes first when the calendar app is not a favourite or not known`() {
        assertEquals(listOf("calendar:1", "Clock"), shape(withCalendarRow(listOf(clock), agenda(1))))
        assertEquals(listOf("calendar:1", "Etar"), shape(withCalendarRow(listOf(etar), agenda(1, pkg = null))))
    }

    @Test
    fun `row lists at most three events`() {
        assertEquals(listOf("calendar:3"), shape(withCalendarRow(emptyList(), agenda(5))))
    }

    @Test
    fun `no agenda or no events means no row`() {
        val rows = listOf(clock)
        assertSame(rows, withCalendarRow(rows, null))
        assertSame(rows, withCalendarRow(rows, agenda(0)))
    }

    @Test
    fun `minutes until an event round up and stop at zero`() {
        assertEquals(1, minutesUntil(begin = 60_000, now = 30_000))
        assertEquals(20, minutesUntil(begin = 20 * 60_000L, now = 0))
        assertEquals(0, minutesUntil(begin = 0, now = 5_000))
    }
}
