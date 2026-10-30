package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChipCrossTest {
    @Test
    fun `the target centres on an 18 dp cross`() {
        val shift = ChipCross.shift(icon = 18f)
        assertEquals(7f, shift, DELTA)
        // Its middle, half a target in from its far end, lands on the cross's middle.
        assertEquals(ChipCross.END_PADDING + 18f / 2, ChipCross.TARGET / 2 - shift, DELTA)
    }

    @Test
    fun `the target stays inside the gap before the next chip`() {
        assertTrue(ChipCross.shift(icon = 18f) < ChipCross.GAP)
    }

    private companion object {
        const val DELTA = 0.001f
    }
}
