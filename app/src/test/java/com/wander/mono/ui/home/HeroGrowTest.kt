package com.wander.mono.ui.home

import com.wander.mono.data.settings.GlanceLines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeroGrowTest {
    @Test
    fun `progress starts at rest, eases in and stops at full`() {
        assertEquals(0f, heroProgress(0f, 100f), 0f)
        assertEquals(0.75f, heroProgress(50f, 100f), 0.0001f)
        assertEquals(1f, heroProgress(100f, 100f), 0f)
        assertEquals(1f, heroProgress(400f, 100f), 0f)
    }

    @Test
    fun `a gentle pull already shows more than its share`() {
        assertTrue(heroProgress(20f, 100f) > 0.2f)
    }

    @Test
    fun `no travel means no hero`() {
        assertEquals(0f, heroProgress(50f, 0f), 0f)
    }

    @Test
    fun `the clock grows toward the display size when it has the room`() {
        assertEquals(1f, heroScale(0f, room = 3f), 0f)
        assertEquals(HERO_MAX_SCALE, heroScale(1f, room = 3f), 0.0001f)
    }

    @Test
    fun `the clock never grows wider than its room`() {
        assertEquals(1.2f, heroScale(1f, room = 1.2f), 0.0001f)
        assertEquals(1f, heroScale(1f, room = 0.8f), 0f)
    }

    @Test
    fun `a settling bounce dips just under the resting size`() {
        assertTrue(heroScale(-0.05f, room = 3f) < 1f)
    }

    @Test
    fun `the simple preset keeps only the date line`() {
        val all = GlanceLines()
        assertEquals(
            GlanceLines(date = true, alarm = false, event = false, battery = false),
            shownLines(all, simple = true),
        )
        assertEquals(all, shownLines(all, simple = false))
        assertEquals(
            GlanceLines(date = false, alarm = false, event = false, battery = false),
            shownLines(GlanceLines(date = false), simple = true),
        )
    }

    @Test
    fun `the summary brings in exactly what the header leaves out`() {
        val shown = GlanceLines(date = true, alarm = false, event = true, battery = false)
        assertEquals(GlanceLines(date = false, alarm = true, event = false, battery = true), summaryLines(shown))
        assertEquals(GlanceLines(false, false, false, false), summaryLines(GlanceLines()))
    }
}
