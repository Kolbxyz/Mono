package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Test

class BlurStepsTest {
    @Test
    fun `blur grows in a few even steps with the drawer`() {
        assertEquals(0, steppedBlur(radius = 120, shown = 0f, steps = 4))
        assertEquals(0, steppedBlur(radius = 120, shown = 0.1f, steps = 4))
        assertEquals(30, steppedBlur(radius = 120, shown = 0.2f, steps = 4))
        assertEquals(60, steppedBlur(radius = 120, shown = 0.5f, steps = 4))
        assertEquals(120, steppedBlur(radius = 120, shown = 1f, steps = 4))
        // A spring's overshoot never blurs past the full radius.
        assertEquals(120, steppedBlur(radius = 120, shown = 1.1f, steps = 4))
        assertEquals(0, steppedBlur(radius = 120, shown = -0.1f, steps = 4))
    }
}
