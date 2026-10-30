package com.wander.mono.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WideLayoutTest {
    @Test
    fun `a tablet or unfolded phone in landscape is wide`() {
        assertTrue(ReadableLayout.isWide(840.dp, 600.dp))
        assertTrue(ReadableLayout.isWide(1280.dp, 800.dp))
    }

    @Test
    fun `a phone on its side keeps the single column however wide`() {
        assertFalse(ReadableLayout.isWide(915.dp, 412.dp))
        assertFalse(ReadableLayout.isWide(840.dp, 479.dp))
    }

    @Test
    fun `below the expanded width class nothing is wide`() {
        assertFalse(ReadableLayout.isWide(839.dp, 1000.dp))
        assertFalse(ReadableLayout.isWide(411.dp, 915.dp))
    }
}
