package com.wander.mono.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayCoverTest {
    @Test
    fun `an overlay covers what is under it as far as it is in`() {
        assertEquals(0f, coverOf(shown = 0f, back = 0f), 0f)
        assertEquals(0.5f, coverOf(shown = 0.5f, back = 0f), 0f)
        assertEquals(1f, coverOf(shown = 1f, back = 0f), 0f)
    }

    @Test
    fun `a spring's overshoot never covers more than fully`() {
        assertEquals(1f, coverOf(shown = 1.08f, back = 0f), 0f)
        assertEquals(0f, coverOf(shown = -0.04f, back = 0f), 0f)
    }

    @Test
    fun `a back swipe hands the layers under it back, up to its share`() {
        val half = coverOf(shown = 1f, back = 0.5f)
        val full = coverOf(shown = 1f, back = 1f)
        assertTrue(half < 1f)
        assertTrue(full < half)
        assertEquals(1f - BACK_GIVE, full, 0.0001f)
    }
}
