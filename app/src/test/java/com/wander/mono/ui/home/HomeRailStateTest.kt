package com.wander.mono.ui.home

import com.wander.mono.ui.drawer.RailLanding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeRailStateTest {
    @Test
    fun `a held letter shows in home's place, star shows home itself`() {
        val rail = HomeRailState()
        rail.hold(2, letters = 5) {}
        assertEquals(2, rail.shown)
        rail.hold(0, letters = 5) {}
        assertEquals(-1, rail.shown)
    }

    @Test
    fun `lifting on a letter lands it for the drawer and keeps it shown until the drawer covers home`() {
        val rail = HomeRailState()
        var opened = false
        rail.hold(3, letters = 5) {}
        rail.hold(-1, letters = 5) { opened = true }
        assertTrue(opened)
        assertEquals(3, rail.shown)
        assertEquals(3, rail.landing.take())
        assertEquals(RailLanding.NONE, rail.landing.take())
        rail.drawerCovers()
        assertEquals(-1, rail.shown)
    }

    @Test
    fun `lifting on star, or before any letter, leaves home as it is`() {
        val rail = HomeRailState()
        var opened = false
        rail.hold(0, letters = 5) {}
        rail.hold(-1, letters = 5) { opened = true }
        rail.hold(-1, letters = 5) { opened = true }
        assertFalse(opened)
        assertEquals(-1, rail.shown)
        assertEquals(RailLanding.NONE, rail.landing.take())
    }
}
