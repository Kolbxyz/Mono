package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClockQueryTest {
    private fun timer(query: String) = ClockQuery.timerSeconds(query)

    private fun alarm(query: String) = ClockQuery.alarmTime(query)

    @Test
    fun `timers with the keyword before or after`() {
        assertEquals(600, timer("timer 10m"))
        assertEquals(600, timer("10 min timer"))
        assertEquals(600, timer("Timer 10"))
        assertEquals(600, timer("set a timer for 10 minutes"))
        assertEquals(45, timer("timer 45s"))
    }

    @Test
    fun `compound and decimal durations`() {
        assertEquals(5400, timer("timer 1h30"))
        assertEquals(5400, timer("timer 1h30m"))
        assertEquals(5400, timer("timer 1 hour and 30 minutes"))
        assertEquals(150, timer("timer 2m30"))
        assertEquals(5400, timer("timer 1.5h"))
        assertEquals(5400, timer("timer 1,5 h"))
        assertEquals(3723, timer("timer 1h 2m 3s"))
    }

    @Test
    fun `a spelled-out unit needs no keyword, a letter does`() {
        assertEquals(300, timer("5 minutes"))
        assertEquals(300, timer("5 min"))
        assertEquals(7200, timer("2 hours"))
        assertNull(timer("5m"))
        assertNull(timer("5s"))
        assertNull(timer("1h30"))
        assertNull(timer("10"))
    }

    @Test
    fun `not timers`() {
        assertNull(timer("timer"))
        assertNull(timer("timer app"))
        assertNull(timer("timer 30 10"))
        assertNull(timer("timer 30m 1h"))
        assertNull(timer("timer 1m 1m"))
        assertNull(timer("timer 0m"))
        assertNull(timer("timer 25h"))
        assertNull(timer("timer 10 parsecs"))
        assertNull(timer("spotify"))
        assertNull(timer("5 km in mi"))
    }

    @Test
    fun `a day is the longest timer`() {
        assertEquals(ClockQuery.MAX_TIMER_SECONDS, timer("timer 24h"))
    }

    @Test
    fun `alarms on the 24-hour clock`() {
        assertEquals(AlarmTime(7, 30), alarm("alarm 7:30"))
        assertEquals(AlarmTime(19, 5), alarm("alarm 19:05"))
        assertEquals(AlarmTime(6, 45), alarm("wake 6:45"))
        assertEquals(AlarmTime(6, 45), alarm("wake me up at 6.45"))
        assertEquals(AlarmTime(7, 0), alarm("Alarm 7"))
        assertEquals(AlarmTime(7, 30), alarm("alarm at 7h30"))
        assertEquals(AlarmTime(0, 15), alarm("alarm 0:15"))
        assertEquals(AlarmTime(7, 30), alarm("7:30 alarm"))
    }

    @Test
    fun `alarms with am and pm`() {
        assertEquals(AlarmTime(7, 0), alarm("alarm 7am"))
        assertEquals(AlarmTime(19, 30), alarm("alarm 7:30 pm"))
        assertEquals(AlarmTime(0, 0), alarm("alarm 12am"))
        assertEquals(AlarmTime(12, 0), alarm("alarm 12pm"))
        assertEquals(AlarmTime(6, 0), alarm("set an alarm for 6 a.m."))
    }

    @Test
    fun `not alarms`() {
        assertNull(alarm("7:30"))
        assertNull(alarm("alarm"))
        assertNull(alarm("alarm clock"))
        assertNull(alarm("alarm 24:00"))
        assertNull(alarm("alarm 7:60"))
        assertNull(alarm("alarm 13pm"))
        assertNull(alarm("alarm 0am"))
        assertNull(alarm("wakelock"))
    }
}
