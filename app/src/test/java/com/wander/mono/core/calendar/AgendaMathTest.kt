package com.wander.mono.core.calendar

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AgendaMathTest {
    private val paris = ZoneId.of("Europe/Paris")
    private val newYork = ZoneId.of("America/New_York")

    private fun at(hour: Int, minute: Int = 0, day: Int = 29, zone: ZoneId = paris) =
        LocalDateTime.of(2026, 10, day, hour, minute).atZone(zone).toInstant().toEpochMilli()

    private fun timed(title: String, begin: Long, end: Long, declined: Boolean = false) =
        CalendarEvent(title.hashCode().toLong(), title, begin, end, allDay = false, declined = declined)

    private fun allDay(title: String, day: Int) = CalendarEvent(
        title.hashCode().toLong(),
        title,
        at(0, day = day),
        at(0, day = day + 1),
        allDay = true,
        declined = false,
    )

    @Test
    fun `all-day events move from UTC midnight to the same date's local midnight`() {
        val utcMidnight = LocalDate.of(2026, 10, 29).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals(at(0, zone = newYork), allDayToLocal(utcMidnight, newYork))
        assertEquals(at(0, zone = paris), allDayToLocal(utcMidnight, paris))
    }

    @Test
    fun `upcoming keeps ongoing and next-24h events and drops past, far and declined ones`() {
        val events = listOf(
            timed("Later", at(14), at(15)),
            timed("Past", at(8), at(9)),
            timed("Ongoing", at(9, 30), at(10, 30)),
            timed("Declined", at(11), at(12), declined = true),
            timed("Tomorrow morning", at(9, day = 30), at(10, day = 30)),
            timed("Too far", at(11, day = 30), at(12, day = 30)),
            allDay("Holiday", 29),
        )
        assertEquals(
            listOf("Holiday", "Ongoing", "Later", "Tomorrow morning"),
            upcomingEvents(events, at(10)).map { it.title },
        )
    }

    @Test
    fun `header shows the next timed event starting within 12 hours`() {
        val events = listOf(
            allDay("Holiday", 29),
            timed("Ongoing", at(9, 30), at(10, 30)),
            timed("Declined", at(10, 15), at(11), declined = true),
            timed("Dinner", at(20), at(21)),
            timed("Standup", at(14), at(14, 15)),
        )
        assertEquals("Standup", headerEvent(events, at(10))?.title)
        assertNull(headerEvent(listOf(timed("Late", at(23), at(23, 30))), at(10)))
    }

    @Test
    fun `next boundary is the earliest end, start, window entry or local midnight`() {
        val now = at(10)
        assertEquals(at(10, 30), nextBoundary(listOf(timed("Ongoing", at(9, 30), at(10, 30))), now, paris))
        assertEquals(at(14), nextBoundary(listOf(timed("Later", at(14), at(15))), now, paris))
        // Tomorrow at 12:00 enters the 24-hour window today at 12:00.
        assertEquals(at(12), nextBoundary(listOf(timed("Lunch", at(12, day = 30), at(13, day = 30))), now, paris))
        assertEquals(at(0, day = 30), nextBoundary(emptyList(), now, paris))
    }
}
