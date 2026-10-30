package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchHighlightTest {
    @Test
    fun `touching and overlapping ranges join, in order`() {
        assertEquals(listOf(0..4), mergedRanges(listOf(3..4, 0..1, 2..2), length = 10))
        assertEquals(listOf(0..3), mergedRanges(listOf(0..2, 1..3), length = 10))
    }

    @Test
    fun `separate ranges stay apart`() {
        assertEquals(listOf(0..0, 7..7), mergedRanges(listOf(7..7, 0..0), length = 11))
    }

    @Test
    fun `ranges are cut to the text and empty ones dropped`() {
        assertEquals(listOf(8..9), mergedRanges(listOf(8..12), length = 10))
        assertEquals(emptyList<IntRange>(), mergedRanges(listOf(IntRange.EMPTY, 12..14), length = 10))
        assertEquals(emptyList<IntRange>(), mergedRanges(listOf(0..3), length = 0))
    }
}
