package com.wander.mono.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadableLayoutTest {
    @Test
    fun `gutter is 24 dp on compact widths and 32 dp from medium on`() {
        assertEquals(24.dp, ReadableLayout.gutter(360.dp))
        assertEquals(24.dp, ReadableLayout.gutter(599.9.dp))
        assertEquals(32.dp, ReadableLayout.gutter(600.dp))
        assertEquals(32.dp, ReadableLayout.gutter(1280.dp))
    }

    @Test
    fun `a phone on its side is compact height, upright or a tablet is not`() {
        assertTrue(ReadableLayout.isCompactHeight(360.dp))
        assertTrue(ReadableLayout.isCompactHeight(479.dp))
        assertFalse(ReadableLayout.isCompactHeight(480.dp))
        assertFalse(ReadableLayout.isCompactHeight(800.dp))
    }
}
