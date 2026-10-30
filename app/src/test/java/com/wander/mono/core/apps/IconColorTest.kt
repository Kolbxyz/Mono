package com.wander.mono.core.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IconColorTest {
    /** Each colour repeated its count, in runs: order does not matter to a histogram. */
    private fun pixels(vararg runs: Pair<Int, Int>): IntArray =
        runs.flatMap { (colour, count) -> List(count) { colour } }.toIntArray()

    @Test
    fun `a solid colour is its own accent`() {
        assertEquals(RED, dominantAccent(pixels(RED to 100)))
        assertEquals(BLUE, dominantAccent(pixels(BLUE to 100)))
    }

    @Test
    fun `the hue covering more of the icon wins`() {
        assertEquals(BLUE, dominantAccent(pixels(BLUE to 70, RED to 30)))
    }

    @Test
    fun `a small vivid logo on a white plate is the accent, not the plate`() {
        assertEquals(GREEN, dominantAccent(pixels(WHITE to 90, GREEN to 10)))
    }

    @Test
    fun `transparent pixels do not vote`() {
        val ghost = 0x00FF0000
        assertEquals(BLUE, dominantAccent(pixels(ghost to 90, BLUE to 10)))
    }

    @Test
    fun `grey, black and see-through icons have no accent`() {
        assertNull(dominantAccent(pixels(GREY to 50, WHITE to 30, BLACK to 20)))
        assertNull(dominantAccent(pixels(0x00000000 to 64)))
        assertNull(dominantAccent(IntArray(0)))
    }

    @Test
    fun `a trace of colour is not enough to be the app's colour`() {
        assertNull(dominantAccent(pixels(GREY to 99, RED to 1)))
    }

    @Test
    fun `the accent is the mean of its hue and comes out opaque`() {
        val darkerRed = 0xFFCC0000.toInt()
        val accent = dominantAccent(pixels(RED to 50, darkerRed to 50))!!
        assertEquals(0xFF, accent ushr 24)
        assertTrue(accent shr 16 and 0xFF in 0xCC..0xFF)
        assertEquals(0, accent and 0xFFFF)
    }

    @Test
    fun `contrast runs from 1 to 21`() {
        assertEquals(21.0, contrastRatio(BLACK, WHITE), 0.01)
        assertEquals(1.0, contrastRatio(RED, RED), 0.0001)
    }

    @Test
    fun `an accent that already stands out is left alone`() {
        assertEquals(BLUE, readableAccent(BLUE, WHITE))
    }

    @Test
    fun `a pale accent on a light surface is darkened just enough`() {
        val yellow = 0xFFFFEB3B.toInt()
        val readable = readableAccent(yellow, WHITE)
        assertTrue(contrastRatio(readable, WHITE) >= MIN_MARK_CONTRAST)
        assertTrue(contrastRatio(readable, WHITE) < 4.5)
    }

    @Test
    fun `a dark accent on a black surface is lightened`() {
        val navy = 0xFF0A1A40.toInt()
        val readable = readableAccent(navy, BLACK)
        assertTrue(contrastRatio(readable, BLACK) >= MIN_MARK_CONTRAST)
        assertTrue(relativeLuminance(readable) > relativeLuminance(navy))
    }

    @Test
    fun `text contrast can be asked for`() {
        assertTrue(contrastRatio(readableAccent(RED, BLACK, minContrast = 7.0), BLACK) >= 7.0)
    }

    private companion object {
        val RED = 0xFFFF0000.toInt()
        val GREEN = 0xFF00C853.toInt()
        val BLUE = 0xFF1565C0.toInt()
        val WHITE = 0xFFFFFFFF.toInt()
        val BLACK = 0xFF000000.toInt()
        val GREY = 0xFF808080.toInt()
    }
}
