package com.wander.mono.core.media

import android.media.session.PlaybackState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PausedCollapseTest {
    @Test
    fun `a playing session is not paused`() {
        assertNull(pausedAt(PlaybackState.STATE_PLAYING, updatedAtMs = 5_000L, nowMs = 9_000L))
    }

    @Test
    fun `a session on its way to playing is not paused`() {
        assertNull(pausedAt(PlaybackState.STATE_BUFFERING, updatedAtMs = 5_000L, nowMs = 9_000L))
        assertNull(pausedAt(PlaybackState.STATE_SKIPPING_TO_NEXT, updatedAtMs = 5_000L, nowMs = 9_000L))
    }

    @Test
    fun `a paused or stopped session counts from when it stopped`() {
        assertEquals(5_000L, pausedAt(PlaybackState.STATE_PAUSED, 5_000L, nowMs = 9_000L))
        assertEquals(5_000L, pausedAt(PlaybackState.STATE_STOPPED, 5_000L, nowMs = 9_000L))
    }

    @Test
    fun `a paused session that never reported a time counts from now`() {
        assertEquals(9_000L, pausedAt(PlaybackState.STATE_PAUSED, 0L, nowMs = 9_000L))
    }

    @Test
    fun `a paused session collapses after the chosen time, never when kept`() {
        assertEquals(5_000L + LINGER, collapseAt(pausedAtMs = 5_000L, lingerMs = LINGER))
        assertNull(collapseAt(pausedAtMs = 5_000L, lingerMs = null))
        assertNull(collapseAt(pausedAtMs = null, lingerMs = LINGER))
    }

    private companion object {
        const val LINGER = 10 * 60 * 1_000L
    }
}
