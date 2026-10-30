package com.wander.mono.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class WallpaperThumbnailTest {
    @Test
    fun `the sample size is the largest power of two that keeps the target width`() {
        assertEquals(4, sampleSize(width = 1440, target = 300))
        assertEquals(2, sampleSize(width = 1440, target = 720))
        assertEquals(1, sampleSize(width = 1440, target = 721))
        assertEquals(1, sampleSize(width = 1440, target = 2000))
    }

    @Test
    fun `an unread width or target decodes at full size`() {
        assertEquals(1, sampleSize(width = 0, target = 300))
        assertEquals(1, sampleSize(width = 1440, target = 0))
    }
}
