package com.wander.mono.ui.home

import com.wander.mono.core.system.WallpaperTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegibilityTest {
    private val white = 0xFFFFFFFF.toInt()
    private val black = 0xFF000000.toInt()
    private val lightSurface = 0xFFFEF7FF.toInt()
    private val darkText = 0xFF1D1B20.toInt()
    private val darkSurface = 0xFF141218.toInt()
    private val lightText = 0xFFE6E0E9.toInt()

    @Test
    fun `contrast matches WCAG extremes`() {
        assertEquals(21f, contrast(black, white), 0.01f)
        assertEquals(1f, contrast(white, white), 0.01f)
    }

    @Test
    fun `blend composites per channel`() {
        assertEquals(white, blend(white, black, 1f))
        assertEquals(black, blend(white, black, 0f))
        assertEquals(0xFF808080.toInt(), blend(white, black, 0.5f))
    }

    @Test
    fun `busy means main colours far apart in brightness`() {
        assertTrue(isBusy(listOf(white, black)))
        assertFalse(isBusy(listOf(0xFF202830.toInt(), 0xFF303840.toInt())))
    }

    @Test
    fun `light theme over a light wallpaper keeps the lightest scrim`() {
        val tone = WallpaperTone(listOf(0xFFF0EEE8.toInt(), 0xFFE0DDD5.toInt()), supportsDarkText = true)
        assertEquals(0.35f, scrimAlpha(tone, lightSurface, darkText), 0.001f)
    }

    @Test
    fun `dark theme over a dark wallpaper uses the base scrim`() {
        val tone = WallpaperTone(listOf(0xFF101820.toInt(), 0xFF203040.toInt()), supportsDarkText = false)
        assertEquals(0.45f, scrimAlpha(tone, darkSurface, lightText), 0.001f)
    }

    @Test
    fun `scrim rises until labels reach 4_5 to 1 on a mismatched wallpaper`() {
        val tone = WallpaperTone(listOf(white), supportsDarkText = true)
        val alpha = scrimAlpha(tone, darkSurface, lightText)
        assertTrue(alpha > 0.45f)
        assertTrue(contrast(lightText, blend(darkSurface, white, alpha)) >= 4.5f)
        assertTrue(contrast(lightText, blend(darkSurface, white, alpha - 0.05f)) < 4.5f)
    }

    @Test
    fun `unknown or busy wallpaper starts from the busy scrim`() {
        assertEquals(0.65f, scrimAlpha(null, darkSurface, lightText), 0.001f)
        val busy = WallpaperTone(listOf(0xFF0A0A0A.toInt(), 0xFFC8D0D8.toInt()), supportsDarkText = false)
        assertTrue(isBusy(busy.colors))
        assertTrue(scrimAlpha(busy, darkSurface, lightText) >= 0.65f)
    }

    @Test
    fun `scrim is full at both edges and clear between header and list`() {
        val stops = scrimStops(height = 1000f, headerBottom = 200f, listTop = 700f, reach = 100f).toMap()
        assertEquals(1f, stops.getValue(0f), 0.001f)
        assertEquals(1f, stops.getValue(1f), 0.001f)
        // 437.5 lies past the header's reach (300) and before the list's (600).
        assertEquals(0f, stops.getValue(0.4375f), 0.001f)
    }

    @Test
    fun `scrim eases without steps`() {
        val strengths = scrimStops(height = 1000f, headerBottom = 200f, listTop = 700f, reach = 100f).map { it.second }
        strengths.zipWithNext().forEach { (a, b) -> assertTrue(kotlin.math.abs(a - b) < 0.5f) }
    }

    @Test
    fun `scrim stays at least half strong when header and list are one reach apart`() {
        assertTrue(scrimStops(1000f, 500f, 550f, 50f).all { (_, strength) -> strength >= 0.5f })
    }
}
