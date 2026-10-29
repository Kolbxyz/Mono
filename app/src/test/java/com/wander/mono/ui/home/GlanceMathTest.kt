package com.wander.mono.ui.home

import com.wander.mono.core.system.BatteryState
import com.wander.mono.data.settings.ClockLook
import com.wander.mono.data.settings.ClockStyle
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GlanceMathTest {
    private val now = 1_000_000_000L
    private val hour = TimeUnit.HOURS.toMillis(1)

    @Test
    fun `an alarm within a day shows`() {
        assertEquals(now + hour, alarmWithinDay(now + hour, now))
        assertEquals(now + 24 * hour, alarmWithinDay(now + 24 * hour, now))
    }

    @Test
    fun `an alarm beyond a day, past, or missing does not`() {
        assertNull(alarmWithinDay(now + 24 * hour + 1, now))
        assertNull(alarmWithinDay(now - 1, now))
        assertNull(alarmWithinDay(null, now))
    }

    @Test
    fun `battery shows only when worth a chip`() {
        assertNull(glanceBattery(BatteryState(60, charging = false)))
        assertEquals(BatteryState(10, charging = false), glanceBattery(BatteryState(10, charging = false)))
        assertNull(glanceBattery(null))
    }

    @Test
    fun `one more hour counts from now, or from a later end`() {
        assertEquals(now + hour, quietEndAfterExtend(until = 0L, now = now))
        assertEquals(now + 3 * hour, quietEndAfterExtend(until = now + 2 * hour, now = now))
        assertEquals(now + hour, quietEndAfterExtend(until = now - hour, now = now))
    }

    @Test
    fun `small and landscape clocks shrink`() {
        assertEquals(88, clockSize(ClockLook(size = 88), compact = false))
        assertEquals(44, clockSize(ClockLook(size = 88, style = ClockStyle.Small), compact = false))
        assertEquals(56, clockSize(ClockLook(size = 88), compact = true))
    }
}
