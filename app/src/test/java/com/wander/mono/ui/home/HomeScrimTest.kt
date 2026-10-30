package com.wander.mono.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class HomeScrimTest {
    private fun stops(gap: Float) = scrimStops(HEIGHT, HEADER, HEADER + gap, REACH)

    private fun strengthAt(stops: List<Pair<Float, Float>>, fraction: Float) =
        stops.minBy { abs(it.first - fraction) }.second

    @Test
    fun `full at both edges`() {
        val stops = stops(gap = 1_200f)
        assertEquals(1f, stops.first().second, 0.001f)
        assertEquals(1f, stops.last().second, 0.001f)
    }

    @Test
    fun `clear between header and list when the gap has room for both falloffs`() {
        val stops = stops(gap = 1_200f)
        assertEquals(0f, strengthAt(stops, (HEADER + 600f) / HEIGHT), 0.001f)
    }

    @Test
    fun `full everywhere once the content meets`() {
        assertTrue(stops(gap = 0f).all { it.second == 1f })
        assertTrue(stops(gap = -50f).all { it.second == 1f })
    }

    @Test
    fun `strength changes continuously as the gap closes`() {
        var previous = stops(gap = 3 * REACH)
        var gap = 3 * REACH
        while (gap > 0f) {
            gap -= 1f
            val next = stops(gap)
            previous.zip(next).forEach { (before, after) -> assertEquals(before.second, after.second, MAX_STEP) }
            previous = next
        }
    }

    @Test
    fun `no strength on a screen with no height to fill`() {
        assertEquals(listOf(0f to 1f, 1f to 1f), scrimStops(0f, HEADER, HEADER + 100f, REACH))
    }

    @Test
    fun `edge mask is clear at both ends and whole past the taper`() {
        val stops = edgeStops(length = 1_000f, taper = 50f)
        assertEquals(0f, stops.first().second, 0.001f)
        assertEquals(0f, stops.last().second, 0.001f)
        assertTrue(stops.filter { it.first in 0.05f..0.95f }.all { it.second == 1f })
    }

    private companion object {
        const val HEIGHT = 2_400f
        const val HEADER = 600f
        const val REACH = 300f

        /** A 1 px change in the gap moves no stop by more than this. */
        const val MAX_STEP = 0.02f
    }
}
