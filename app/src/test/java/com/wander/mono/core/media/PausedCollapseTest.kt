package com.wander.mono.core.media

import android.media.session.PlaybackState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PausedCollapseTest {
    @Test
    fun `a playing session never collapses`() {
        assertNull(collapseAt(PlaybackState.STATE_PLAYING, updatedAtMs = 5_000L, nowMs = 9_000L))
    }

    @Test
    fun `a session on its way to playing never collapses`() {
        assertNull(collapseAt(PlaybackState.STATE_BUFFERING, updatedAtMs = 5_000L, nowMs = 9_000L))
        assertNull(collapseAt(PlaybackState.STATE_SKIPPING_TO_NEXT, updatedAtMs = 5_000L, nowMs = 9_000L))
    }

    @Test
    fun `a paused or stopped session collapses ten minutes after it stopped`() {
        assertEquals(5_000L + PAUSED_LINGER_MS, collapseAt(PlaybackState.STATE_PAUSED, 5_000L, nowMs = 9_000L))
        assertEquals(5_000L + PAUSED_LINGER_MS, collapseAt(PlaybackState.STATE_STOPPED, 5_000L, nowMs = 9_000L))
    }

    @Test
    fun `a paused session that never reported a time counts from now`() {
        assertEquals(9_000L + PAUSED_LINGER_MS, collapseAt(PlaybackState.STATE_PAUSED, 0L, nowMs = 9_000L))
    }
}
