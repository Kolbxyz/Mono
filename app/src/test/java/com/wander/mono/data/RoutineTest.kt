package com.wander.mono.data

import com.wander.mono.core.apps.AppKey
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineTest {
    private val news = AppKey("org.news", "org.news.Main", 0)
    private val books = AppKey("org.books", "org.books.Main", 0)
    private val morning = Routine(start = 7 * 60, end = 9 * 60, apps = setOf(news))
    private val night = Routine(start = 22 * 60, end = 1 * 60, apps = setOf(books))

    @Test
    fun `a window is on from its start up to its end`() {
        assertFalse(morning.isOn(7 * 60 - 1))
        assertTrue(morning.isOn(7 * 60))
        assertTrue(morning.isOn(9 * 60 - 1))
        assertFalse(morning.isOn(9 * 60))
    }

    @Test
    fun `a window ending before it starts runs past midnight`() {
        assertTrue(night.isOn(23 * 60))
        assertTrue(night.isOn(30))
        assertFalse(night.isOn(1 * 60))
        assertFalse(night.isOn(12 * 60))
    }

    @Test
    fun `a window ending where it starts is never on`() {
        assertFalse(Routine(600, 600, setOf(news)).isOn(600))
    }

    @Test
    fun `the apps of every routine on are put first`() {
        val routines = listOf(morning, night, Routine(8 * 60, 10 * 60, setOf(books)))
        assertEquals(setOf(news, books), routines.appsAt(LocalTime.of(8, 30)))
        assertEquals(emptySet<AppKey>(), routines.appsAt(LocalTime.of(12, 0)))
    }

    @Test
    fun `the next boundary is the nearest start or end, the next day once all have passed`() {
        val routines = listOf(morning, night)
        assertEquals(30 * MINUTE, routines.millisToNextBoundary(LocalTime.of(6, 30)))
        assertEquals(60 * MINUTE, routines.millisToNextBoundary(LocalTime.of(0, 0)))
        // At a boundary exactly, the next one is wanted, not this one again.
        assertEquals(2 * 60 * MINUTE, routines.millisToNextBoundary(LocalTime.of(7, 0)))
        assertEquals(MINUTE - 1_500, listOf(morning).millisToNextBoundary(LocalTime.of(8, 59, 1, 500_000_000)))
        assertEquals(22 * 60 * MINUTE, listOf(morning).millisToNextBoundary(LocalTime.of(9, 0)))
    }

    @Test
    fun `nothing changes without routines or with empty windows`() {
        assertNull(emptyList<Routine>().millisToNextBoundary(LocalTime.NOON))
        assertNull(listOf(Routine(600, 600, setOf(news))).millisToNextBoundary(LocalTime.NOON))
    }

    @Test
    fun `a routine survives encoding and decoding`() {
        listOf(morning, night, Routine(0, 1439, emptySet())).forEach { assertEquals(it, Routine.decode(it.encode())) }
    }

    @Test
    fun `damaged lines are not routines`() {
        assertNull(Routine.decode(""))
        assertNull(Routine.decode("420|540"))
        assertNull(Routine.decode("420|1440|"))
        assertNull(Routine.decode("-1|540|"))
        assertNull(Routine.decode("420|540|not a key"))
    }

    private companion object {
        const val MINUTE = 60_000L
    }
}
