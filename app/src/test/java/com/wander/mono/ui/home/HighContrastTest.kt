package com.wander.mono.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HighContrastTest {
    @Test
    fun `the legibility alpha is used as is by default`() {
        assertEquals(0.45f, homeScrimAlpha(0.45f, textShadow = false, highContrast = false), DELTA)
    }

    @Test
    fun `a text shadow replaces the scrim`() {
        assertEquals(0f, homeScrimAlpha(0.45f, textShadow = true, highContrast = false), DELTA)
    }

    @Test
    fun `high contrast raises the scrim by a fifth`() {
        assertEquals(0.65f, homeScrimAlpha(0.45f, textShadow = false, highContrast = true), DELTA)
    }

    @Test
    fun `high contrast keeps a scrim even under a text shadow`() {
        assertEquals(0.55f, homeScrimAlpha(0.35f, textShadow = true, highContrast = true), DELTA)
    }

    @Test
    fun `the raised scrim never goes past opaque`() {
        assertEquals(1f, homeScrimAlpha(0.9f, textShadow = false, highContrast = true), DELTA)
    }

    private companion object {
        const val DELTA = 0.001f
    }
}
