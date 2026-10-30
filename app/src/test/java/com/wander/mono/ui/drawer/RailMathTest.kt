package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RailMathTest {
    @Test
    fun `finger picks the slot it is in and clamps past either end`() {
        assertEquals(0, RailMath.letterAt(y = 0f, top = 0f, height = 100f, count = 4))
        assertEquals(0, RailMath.letterAt(y = 24.9f, top = 0f, height = 100f, count = 4))
        assertEquals(1, RailMath.letterAt(y = 25f, top = 0f, height = 100f, count = 4))
        assertEquals(3, RailMath.letterAt(y = 99f, top = 0f, height = 100f, count = 4))
        assertEquals(0, RailMath.letterAt(y = -40f, top = 0f, height = 100f, count = 4))
        assertEquals(3, RailMath.letterAt(y = 180f, top = 0f, height = 100f, count = 4))
    }

    @Test
    fun `slots start below the letters' top padding`() {
        assertEquals(0, RailMath.letterAt(y = 30f, top = 8f, height = 100f, count = 4))
        assertEquals(0, RailMath.letterAt(y = 32.9f, top = 8f, height = 100f, count = 4))
        assertEquals(1, RailMath.letterAt(y = 33f, top = 8f, height = 100f, count = 4))
        assertEquals(3, RailMath.letterAt(y = 107f, top = 8f, height = 100f, count = 4))
        assertEquals(0, RailMath.letterAt(y = 2f, top = 8f, height = 100f, count = 4))
    }

    @Test
    fun `no letter on an empty or unmeasured rail`() {
        assertEquals(-1, RailMath.letterAt(y = 10f, top = 0f, height = 100f, count = 0))
        assertEquals(-1, RailMath.letterAt(y = 10f, top = 0f, height = 0f, count = 4))
    }

    @Test
    fun `finger slot is zero at the centre of the first letter`() {
        assertEquals(0f, RailMath.fingerSlot(y = 12.5f, top = 0f, height = 100f, count = 4), DELTA)
        assertEquals(2f, RailMath.fingerSlot(y = 62.5f, top = 0f, height = 100f, count = 4), DELTA)
        assertEquals(0f, RailMath.fingerSlot(y = 20.5f, top = 8f, height = 100f, count = 4), DELTA)
    }

    @Test
    fun `bubble centres on the finger but stays on the rail`() {
        assertEquals(70f, RailMath.bubbleTop(y = 100f, size = 60f, railHeight = 400f), DELTA)
        assertEquals(0f, RailMath.bubbleTop(y = 10f, size = 60f, railHeight = 400f), DELTA)
        assertEquals(340f, RailMath.bubbleTop(y = 395f, size = 60f, railHeight = 400f), DELTA)
        // A rail shorter than the bubble pins it to the top.
        assertEquals(0f, RailMath.bubbleTop(y = 20f, size = 60f, railHeight = 40f), DELTA)
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
    fun `every letter is drawn on a tall rail, every other or third on a short one`() {
        assertEquals(1, RailMath.labelStride(height = 600f, count = 26, minSlot = 16f))
        assertEquals(2, RailMath.labelStride(height = 260f, count = 26, minSlot = 16f))
        assertEquals(3, RailMath.labelStride(height = 150f, count = 26, minSlot = 16f))
        assertEquals(1, RailMath.labelStride(height = 0f, count = 26, minSlot = 16f))
        assertEquals(1, RailMath.labelStride(height = 300f, count = 0, minSlot = 16f))
    }

    @Test
    fun `travel counts only the slide toward the list, mirrored right to left`() {
        assertEquals(30f, RailMath.travel(downX = 40f, x = 10f, mirror = 1f), DELTA)
        assertEquals(0f, RailMath.travel(downX = 40f, x = 70f, mirror = 1f), DELTA)
        assertEquals(30f, RailMath.travel(downX = 10f, x = 40f, mirror = -1f), DELTA)
        assertEquals(0f, RailMath.travel(downX = 40f, x = 10f, mirror = -1f), DELTA)
    }

    @Test
    fun `bow follows the finger at first and never passes its limit`() {
        assertEquals(0f, RailMath.bow(travel = 0f, limit = 100f), DELTA)
        assertEquals(0f, RailMath.bow(travel = -20f, limit = 100f), DELTA)
        // A short slide moves the letter almost as far as the finger.
        assertTrue(RailMath.bow(travel = 5f, limit = 100f) > 4.7f)
        assertTrue(RailMath.bow(travel = 10_000f, limit = 100f) < 100f)
        val samples = (0..50).map { it * 10f }
        samples.zipWithNext().forEach { (near, far) ->
            assertTrue(RailMath.bow(near, limit = 100f) < RailMath.bow(far, limit = 100f))
        }
    }

    @Test
    fun `letters under the finger move furthest and the wave scales with the bow`() {
        assertEquals(40f, RailMath.letterOffset(index = 3, slot = 3f, bow = 40f), DELTA)
        assertEquals(20f, RailMath.letterOffset(index = 5, slot = 3f, bow = 40f), DELTA)
        assertEquals(0f, RailMath.letterOffset(index = 9, slot = 3f, bow = 40f), DELTA)
        assertEquals(0f, RailMath.letterOffset(index = 3, slot = 3f, bow = 0f), DELTA)
    }

    @Test
    fun `star and the first letter both jump to the top, later letters to their own item`() {
        assertEquals(0, RailMath.listItemFor(railIndex = 0))
        assertEquals(0, RailMath.listItemFor(railIndex = 1))
        assertEquals(4, RailMath.listItemFor(railIndex = 5))
    }

    @Test
    fun `rail shows star only at the very top and clamps past the letters`() {
        assertEquals(0, RailMath.railIndexFor(firstItem = 0, firstOffset = 0, letters = 5))
        assertEquals(1, RailMath.railIndexFor(firstItem = 0, firstOffset = 12, letters = 5))
        assertEquals(4, RailMath.railIndexFor(firstItem = 3, firstOffset = 0, letters = 5))
        // Work and private space cards after the last letter keep the last letter.
        assertEquals(5, RailMath.railIndexFor(firstItem = 6, firstOffset = 0, letters = 5))
    }

    @Test
    fun `wave faces the list from whichever edge the rail is on`() {
        assertEquals(1f, RailMath.mirror(rtl = false, atStart = false), DELTA)
        assertEquals(-1f, RailMath.mirror(rtl = false, atStart = true), DELTA)
        assertEquals(-1f, RailMath.mirror(rtl = true, atStart = false), DELTA)
        assertEquals(1f, RailMath.mirror(rtl = true, atStart = true), DELTA)
    }

    @Test
    fun `bubble sits on the list's side, clear of the bowed letter`() {
        assertEquals(-100f, RailMath.bubbleX(mirror = 1f, gap = 16f, size = 72f, bow = 12f), DELTA)
        assertEquals(100f, RailMath.bubbleX(mirror = -1f, gap = 16f, size = 72f, bow = 12f), DELTA)
    }

    private companion object {
        const val DELTA = 1e-4f
    }
}
