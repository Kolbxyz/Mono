package com.wander.mono.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackProgressTest {
    @Test
    fun `an unknown position or duration gives no bar`() {
        assertNull(playbackProgress(-1L, 1_000L, 1f, 0L, isPlaying = true))
        assertNull(playbackProgress(0L, 0L, 1f, 0L, isPlaying = true))
    }

    @Test
    fun `playing extrapolates from the last report at the playback speed`() {
        val progress = playbackProgress(10_000L, 100_000L, 2f, updatedAtMs = 1_000L, isPlaying = true)!!
        assertEquals(0.3f, progress.fractionAt(11_000L), DELTA)
    }

    @Test
    fun `paused stays where it was`() {
        val progress = playbackProgress(10_000L, 100_000L, 1f, updatedAtMs = 1_000L, isPlaying = false)!!
        assertEquals(0.1f, progress.fractionAt(60_000L), DELTA)
    }

    @Test
    fun `the fraction is clamped to the track`() {
        val progress = playbackProgress(90_000L, 100_000L, 1f, updatedAtMs = 0L, isPlaying = true)!!
        assertEquals(1f, progress.fractionAt(500_000L), DELTA)
    }

    @Test
    fun `the next tick lands on the position's next whole second`() {
        val progress = playbackProgress(10_250L, 100_000L, 1f, updatedAtMs = 0L, isPlaying = true)!!
        assertEquals(750L, progress.msToNextSecondAt(0L))
        assertEquals(1_000L, progress.msToNextSecondAt(750L))
    }

    @Test
    fun `a faster speed reaches the next second sooner`() {
        val progress = playbackProgress(10_000L, 100_000L, 2f, updatedAtMs = 0L, isPlaying = true)!!
        assertEquals(500L, progress.msToNextSecondAt(0L))
    }

    private companion object {
        const val DELTA = 0.0001f
    }
}
