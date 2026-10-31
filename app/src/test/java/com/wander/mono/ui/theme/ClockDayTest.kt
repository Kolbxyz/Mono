package com.wander.mono.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ClockDayTest {
    @Test
    fun `the day has five phases at fixed hours`() {
        assertEquals(DayPhase.Night, dayPhase(0))
        assertEquals(DayPhase.Night, dayPhase(5))
        assertEquals(DayPhase.Morning, dayPhase(6))
        assertEquals(DayPhase.Midday, dayPhase(11))
        assertEquals(DayPhase.Evening, dayPhase(17))
        assertEquals(DayPhase.Late, dayPhase(21))
        assertEquals(DayPhase.Late, dayPhase(23))
    }

    @Test
    fun `the shifted weight stays on the font's axis`() {
        assertEquals(460, dayWeight(400, DayPhase.Midday, 100..1000))
        assertEquals(200, dayWeight(250, DayPhase.Night, 200..700))
        assertEquals(400, dayWeight(400, DayPhase.Midday, 400..400))
    }

    @Test
    fun `easing weights step by ten`() {
        assertEquals(450, steppedWeight(457))
        assertEquals(400, steppedWeight(400))
    }
}
