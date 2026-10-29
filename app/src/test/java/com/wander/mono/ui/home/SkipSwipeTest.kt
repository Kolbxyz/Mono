package com.wander.mono.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SkipSwipeTest {
    private fun decide(offset: Float, velocity: Float = 0f, canNext: Boolean = true, canPrevious: Boolean = true) =
        skipDirectionFor(offset, velocity, DISTANCE, FLING, canNext, canPrevious)

    @Test
    fun `a short slow drag does nothing`() {
        assertNull(decide(offset = -50f, velocity = -100f))
    }

    @Test
    fun `dragging left past the distance skips to next, right to previous`() {
        assertEquals(SkipDirection.Next, decide(offset = -DISTANCE))
        assertEquals(SkipDirection.Previous, decide(offset = DISTANCE + 1f))
    }

    @Test
    fun `a fling skips even when the drag is short`() {
        assertEquals(SkipDirection.Next, decide(offset = -10f, velocity = -FLING))
        assertEquals(SkipDirection.Previous, decide(offset = 5f, velocity = FLING * 2))
    }

    @Test
    fun `flicking back against a long drag cancels`() {
        assertNull(decide(offset = -DISTANCE * 2, velocity = FLING))
    }

    @Test
    fun `a direction the session does not support springs back`() {
        assertNull(decide(offset = -DISTANCE * 2, canNext = false))
        assertNull(decide(offset = DISTANCE * 2, canPrevious = false))
        assertEquals(SkipDirection.Next, decide(offset = -DISTANCE * 2, canPrevious = false))
    }

    private companion object {
        const val DISTANCE = 200f
        const val FLING = 1_500f
    }
}
