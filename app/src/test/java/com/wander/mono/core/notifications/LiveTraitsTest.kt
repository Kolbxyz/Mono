package com.wander.mono.core.notifications

import androidx.core.app.NotificationCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LiveTraitsTest {
    private fun traits(
        category: String? = null,
        ongoing: Boolean = true,
        summary: Boolean = false,
        media: Boolean = false,
        chronometer: Boolean = false,
        clock: Boolean = false,
        liveUpdate: Boolean = false,
        progress: LiveProgress? = null,
    ) = LiveTraits(category, ongoing, summary, media, chronometer, clock, liveUpdate, progress)

    private val half = LiveProgress.Determinate(0.5f)

    @Test
    fun `only ongoing notifications that are not media, calls or summaries are live`() {
        assertNull(liveKindOf(traits(ongoing = false, progress = half)))
        assertNull(liveKindOf(traits(summary = true, progress = half)))
        assertNull(liveKindOf(traits(media = true, progress = half)))
        assertNull(liveKindOf(traits(category = NotificationCompat.CATEGORY_TRANSPORT, progress = half)))
        assertNull(liveKindOf(traits(category = NotificationCompat.CATEGORY_CALL, chronometer = true)))
        assertNull(liveKindOf(traits()))
    }

    @Test
    fun `navigation goes by its category, whatever else it shows`() {
        val kind = liveKindOf(traits(category = NotificationCompat.CATEGORY_NAVIGATION, chronometer = true))
        assertEquals(LiveActivityKind.Navigation, kind)
    }

    @Test
    fun `a chronometer, the stopwatch category or the clock app make a timer`() {
        assertEquals(LiveActivityKind.Timer, liveKindOf(traits(chronometer = true)))
        assertEquals(LiveActivityKind.Timer, liveKindOf(traits(category = NotificationCompat.CATEGORY_STOPWATCH)))
        assertEquals(LiveActivityKind.Timer, liveKindOf(traits(clock = true)))
    }

    @Test
    fun `a live update with a progress stays a progress even with a chronometer`() {
        val ride = traits(liveUpdate = true, chronometer = true, progress = half)
        assertEquals(LiveActivityKind.Progress, liveKindOf(ride))
        assertEquals(LiveActivityKind.Progress, liveKindOf(traits(liveUpdate = true)))
    }

    @Test
    fun `an unknown-length progress needs the progress category`() {
        assertNull(liveKindOf(traits(progress = LiveProgress.Indeterminate)))
        val download = traits(category = NotificationCompat.CATEGORY_PROGRESS, progress = LiveProgress.Indeterminate)
        assertEquals(LiveActivityKind.Progress, liveKindOf(download))
        assertEquals(LiveActivityKind.Progress, liveKindOf(traits(progress = half)))
    }

    @Test
    fun `progress comes from the extras and is clamped`() {
        assertEquals(LiveProgress.Determinate(0.25f), progressOf(25, 100, indeterminate = false))
        assertEquals(LiveProgress.Determinate(1f), progressOf(150, 100, indeterminate = false))
        assertEquals(LiveProgress.Indeterminate, progressOf(0, 0, indeterminate = true))
        assertNull(progressOf(0, 0, indeterminate = false))
    }

    @Test
    fun `one row per app and kind, a running timer first, then the oldest`() {
        val clock = PackageUser("com.android.deskclock", 0)
        val files = PackageUser("org.files", 0)
        val paused = LiveCandidate("a", clock, LiveActivityKind.Timer, ticking = false, startedAt = 1)
        val running = LiveCandidate("b", clock, LiveActivityKind.Timer, ticking = true, startedAt = 5)
        val newer = LiveCandidate("c", files, LiveActivityKind.Progress, ticking = false, startedAt = 9)
        val older = LiveCandidate("d", files, LiveActivityKind.Progress, ticking = false, startedAt = 2)
        assertEquals(listOf(running, older), chooseLive(listOf(paused, running, newer, older)))
    }
}
