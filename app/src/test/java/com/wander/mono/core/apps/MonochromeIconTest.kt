package com.wander.mono.core.apps

import org.junit.Assert.assertEquals
import org.junit.Test

class MonochromeIconTest {
    @Test
    fun `monochrome inset is a quarter of the visible size on each side`() {
        assertEquals(0, monochromeInset(0, 0.25f))
        assertEquals(30, monochromeInset(120, 0.25f))
        assertEquals(21, monochromeInset(84, 0.25f))
    }
}
