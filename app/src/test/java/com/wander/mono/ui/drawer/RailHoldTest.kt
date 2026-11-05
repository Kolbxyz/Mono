package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RailHoldTest {
    @Test
    fun `nothing held shows every group`() {
        val hold = RailHold()
        assertEquals(-1, hold.heldGroup(lead = 1, letters = 3))
        assertTrue(hold.shows(0, lead = 1, letters = 3))
        assertTrue(hold.shows(2, lead = 1, letters = 3))
    }

    @Test
    fun `a held letter keeps only its own group, after the lead groups`() {
        val hold = RailHold().apply { entry = 2 }
        assertEquals(2, hold.heldGroup(lead = 1, letters = 3))
        assertTrue(hold.shows(2, lead = 1, letters = 3))
        assertFalse(hold.shows(0, lead = 1, letters = 3))
        assertFalse(hold.shows(1, lead = 1, letters = 3))
    }

    @Test
    fun `the star and the lock hold no letter`() {
        assertEquals(-1, RailHold().apply { entry = 0 }.heldGroup(lead = 0, letters = 3))
        assertEquals(-1, RailHold().apply { entry = 4 }.heldGroup(lead = 0, letters = 3))
    }
}
