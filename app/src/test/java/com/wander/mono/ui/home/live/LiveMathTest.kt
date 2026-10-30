package com.wander.mono.ui.home.live

import org.junit.Assert.assertEquals
import org.junit.Test

class LiveMathTest {
    @Test
    fun `a stopwatch counts whole seconds up from its base`() {
        assertEquals(0L, chronometerSeconds(base = 10_000, now = 10_999, countsDown = false))
        assertEquals(65L, chronometerSeconds(base = 10_000, now = 75_500, countsDown = false))
    }

    @Test
    fun `a timer counts down and goes negative once past zero`() {
        assertEquals(59L, chronometerSeconds(base = 60_000, now = 500, countsDown = true))
        assertEquals(0L, chronometerSeconds(base = 60_000, now = 60_400, countsDown = true))
        assertEquals(-2L, chronometerSeconds(base = 60_000, now = 62_100, countsDown = true))
    }

    @Test
    fun `the next redraw lands on the next shown second`() {
        assertEquals(1_000L, msUntilNextSecond(base = 10_000, now = 12_000))
        assertEquals(250L, msUntilNextSecond(base = 10_000, now = 12_750))
        assertEquals(250L, msUntilNextSecond(base = 10_000, now = 9_750))
    }

    @Test
    fun `percentages round down and survive float error`() {
        assertEquals(29, percentOf(29f / 100))
        assertEquals(99, percentOf(0.995f))
        assertEquals(100, percentOf(1f))
        assertEquals(0, percentOf(-0.5f))
    }
}
