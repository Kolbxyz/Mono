package com.wander.mono.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChoiceRowWidthsTest {
    @Test
    fun `buttons share the row equally when the widest allows it`() {
        assertEquals(listOf(34, 33, 33), rowWidths(listOf(20, 30, 10), width = 104, gap = 2))
    }

    @Test
    fun `uneven labels keep their own width plus an even share of the rest`() {
        assertEquals(listOf(62, 22), rowWidths(listOf(60, 20), width = 86, gap = 2))
    }

    @Test
    fun `labels that do not fit at full width wrap instead of shrinking`() {
        assertNull(rowWidths(listOf(60, 60), width = 100, gap = 2))
    }
}
