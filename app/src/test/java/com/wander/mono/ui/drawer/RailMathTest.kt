package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RailMathTest {
    @Test
    fun `finger picks the slot it is in and clamps past either end`() {
        assertEquals(0, RailMath.letterAt(y = 0f, height = 100f, count = 4))
        assertEquals(0, RailMath.letterAt(y = 24.9f, height = 100f, count = 4))
        assertEquals(1, RailMath.letterAt(y = 25f, height = 100f, count = 4))
        assertEquals(3, RailMath.letterAt(y = 99f, height = 100f, count = 4))
        assertEquals(0, RailMath.letterAt(y = -40f, height = 100f, count = 4))
        assertEquals(3, RailMath.letterAt(y = 180f, height = 100f, count = 4))
    }

    @Test
    fun `no letter on an empty or unmeasured rail`() {
        assertEquals(-1, RailMath.letterAt(y = 10f, height = 100f, count = 0))
        assertEquals(-1, RailMath.letterAt(y = 10f, height = 0f, count = 4))
    }

    @Test
    fun `finger slot is zero at the centre of the first letter`() {
        assertEquals(0f, RailMath.fingerSlot(y = 12.5f, height = 100f, count = 4), DELTA)
        assertEquals(2f, RailMath.fingerSlot(y = 62.5f, height = 100f, count = 4), DELTA)
    }

    @Test
    fun `wave peaks under the finger and is flat from the reach on`() {
        assertEquals(1f, RailMath.waveFraction(0f), DELTA)
        assertEquals(0.5f, RailMath.waveFraction(RailMath.WAVE_REACH / 2), DELTA)
        assertEquals(0f, RailMath.waveFraction(RailMath.WAVE_REACH), DELTA)
        assertEquals(0f, RailMath.waveFraction(RailMath.WAVE_REACH * 3), DELTA)
    }

    @Test
    fun `wave is symmetric and falls steadily away from the finger`() {
        val samples = (0..40).map { it / 10f }
        samples.forEach { d -> assertEquals(RailMath.waveFraction(d), RailMath.waveFraction(-d), DELTA) }
        samples.zipWithNext().forEach { (near, far) ->
            assertTrue(RailMath.waveFraction(near) >= RailMath.waveFraction(far))
        }
    }

    @Test
    fun `current section is the last one starting at or before the first visible item`() {
        val sections = listOf(Section("A", 0), Section("C", 4), Section("F", 9))
        assertEquals(0, RailMath.sectionIndexFor(sections, 0))
        assertEquals(0, RailMath.sectionIndexFor(sections, 3))
        assertEquals(1, RailMath.sectionIndexFor(sections, 4))
        assertEquals(2, RailMath.sectionIndexFor(sections, 30))
        assertEquals(-1, RailMath.sectionIndexFor(emptyList(), 3))
    }

    @Test
    fun `every letter is drawn on a tall rail, every other or third on a short one`() {
        assertEquals(1, RailMath.labelStride(height = 600f, count = 26, minSlot = 16f))
        assertEquals(2, RailMath.labelStride(height = 260f, count = 26, minSlot = 16f))
        assertEquals(3, RailMath.labelStride(height = 150f, count = 26, minSlot = 16f))
        assertEquals(1, RailMath.labelStride(height = 0f, count = 26, minSlot = 16f))
        assertEquals(1, RailMath.labelStride(height = 300f, count = 0, minSlot = 16f))
    }

    private companion object {
        const val DELTA = 1e-4f
    }
}
