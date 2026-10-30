package com.wander.mono.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SegmentedStripsTest {
    @Test
    fun `strips stack top down with the gap between them only`() {
        assertEquals(
            listOf(Strip(top = 0, height = 10), Strip(top = 12, height = 20), Strip(top = 34, height = 5)),
            stackStrips(listOf(10, 20, 5), gap = 2),
        )
    }

    @Test
    fun `no rows, no strips`() {
        assertEquals(emptyList<Strip>(), stackStrips(emptyList(), gap = 2))
    }

    @Test
    fun `strips that trade height at the same total are a different layout`() {
        // The redraw depends on this: the group's size alone does not change.
        assertNotEquals(stackStrips(listOf(10, 20), gap = 2), stackStrips(listOf(20, 10), gap = 2))
    }
}
