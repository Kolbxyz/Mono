package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchPullTest {
    @Test
    fun `fires once when the leftover upward drag passes the threshold`() {
        val pull = SearchPull(threshold = 50f)
        assertFalse(pull.drag(consumed = 0f, leftover = -30f))
        assertTrue(pull.drag(consumed = 0f, leftover = -30f))
        assertFalse(pull.drag(consumed = 0f, leftover = -100f))
    }

    @Test
    fun `the list moving starts the count afresh`() {
        val pull = SearchPull(threshold = 50f)
        assertFalse(pull.drag(consumed = 0f, leftover = -40f))
        assertFalse(pull.drag(consumed = -10f, leftover = -5f))
        assertFalse(pull.drag(consumed = 0f, leftover = -40f))
        assertTrue(pull.drag(consumed = 0f, leftover = -20f))
    }

    @Test
    fun `a downward drag counts nothing and resets`() {
        val pull = SearchPull(threshold = 50f)
        assertFalse(pull.drag(consumed = 0f, leftover = -40f))
        assertFalse(pull.drag(consumed = 0f, leftover = 10f))
        assertFalse(pull.drag(consumed = 0f, leftover = -40f))
    }

    @Test
    fun `each gesture can fire once`() {
        val pull = SearchPull(threshold = 50f)
        assertTrue(pull.drag(consumed = 0f, leftover = -60f))
        pull.end()
        val fired = listOf(pull.drag(0f, -30f), pull.drag(0f, -30f))
        assertEquals(listOf(false, true), fired)
    }
}
