package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Test

class ScrubBandTest {
    @Test
    fun `a strip within the limit is kept from the back gesture whole`() {
        val rect = ScrubBand.exclusion(width = 80f, height = 300f, max = 500f)
        assertEquals(0f, rect.top, DELTA)
        assertEquals(300f, rect.bottom, DELTA)
        assertEquals(80f, rect.right, DELTA)
    }

    @Test
    fun `a taller strip keeps only the limit, centred on it`() {
        val rect = ScrubBand.exclusion(width = 80f, height = 1000f, max = 400f)
        assertEquals(300f, rect.top, DELTA)
        assertEquals(700f, rect.bottom, DELTA)
    }

    @Test
    fun `a strip not laid out yet excludes nothing`() {
        val rect = ScrubBand.exclusion(width = 0f, height = 0f, max = 400f)
        assertEquals(0f, rect.height, DELTA)
    }

    private companion object {
        const val DELTA = 0.001f
    }
}
