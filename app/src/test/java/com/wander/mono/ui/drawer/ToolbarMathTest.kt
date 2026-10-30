package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolbarMathTest {
    @Test
    fun `scrolling on adds up, turning back starts over`() {
        assertEquals(-30f, ToolbarMath.travel(-10f, -20f), DELTA)
        assertEquals(5f, ToolbarMath.travel(-30f, 5f), DELTA)
    }

    @Test
    fun `hides past the threshold toward the end and shows past it back`() {
        assertFalse(ToolbarMath.visible(current = true, travel = -24f, threshold = 24f))
        assertTrue(ToolbarMath.visible(current = true, travel = -10f, threshold = 24f))
        assertFalse(ToolbarMath.visible(current = false, travel = 10f, threshold = 24f))
        assertTrue(ToolbarMath.visible(current = false, travel = 30f, threshold = 24f))
    }

    private companion object {
        const val DELTA = 0.001f
    }
}
