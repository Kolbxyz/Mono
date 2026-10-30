package com.wander.mono.ui.home

import androidx.compose.ui.unit.Density
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ListPlacementTest {
    private val density = Density(1f)

    private fun positions(fraction: Float, total: Int, vararg sizes: Int): IntArray {
        val out = IntArray(sizes.size)
        with(ListPlacement(fraction)) { density.arrange(total, sizes, out) }
        return out
    }

    @Test
    fun `the default sits the rows at the bottom, touching`() {
        assertArrayEquals(intArrayOf(70, 80), positions(1f, 100, 10, 20))
    }

    @Test
    fun `zero sits them against the top and a share in between`() {
        assertArrayEquals(intArrayOf(0, 10), positions(0f, 100, 10, 20))
        assertArrayEquals(intArrayOf(35, 45), positions(0.5f, 100, 10, 20))
    }

    @Test
    fun `an overshooting spring and a list taller than its room stay in it`() {
        assertArrayEquals(intArrayOf(70, 80), positions(1.2f, 100, 10, 20))
        assertArrayEquals(intArrayOf(0, 10), positions(-0.1f, 100, 10, 20))
        assertArrayEquals(intArrayOf(0, 80), positions(1f, 100, 80, 80))
    }
}
