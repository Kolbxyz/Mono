package com.wander.mono.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SliderStopsTest {
    @Test
    fun `stops run from the first value every step to the last`() {
        assertEquals(listOf(100, 200, 300, 400), sliderStops(100..400, step = 100))
    }

    @Test
    fun `a last value off the step is still a stop`() {
        assertEquals(listOf(0, 3, 6, 7), sliderStops(0..7, step = 3))
    }

    @Test
    fun `a one-value range has that one stop`() {
        assertEquals(listOf(5), sliderStops(5..5, step = 10))
    }
}
