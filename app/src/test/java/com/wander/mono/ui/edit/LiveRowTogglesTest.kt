package com.wander.mono.ui.edit

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.media.MediaState
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.data.LiveRowKind
import com.wander.mono.data.LiveRowRef
import com.wander.mono.ui.home.unlessHidden
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class LiveRowTogglesTest {
    private val wanda = app("com.wander.android", "Wanda")
    private val clock = app("com.android.deskclock", "Clock")
    private val workWanda = app("com.wander.android", "Wanda", serial = 10, main = false)

    private fun app(pkg: String, label: String, serial: Long = 0, main: Boolean = true) =
        LaunchableApp(AppKey(pkg, "$pkg.Main", serial), label, main)

    private val playing = MediaState.Active(
        PlayingMedia(
            "com.wander.android",
            "Song",
            null,
            art = null,
            artColor = null,
            isPlaying = true,
            canSkipNext = false,
            canSkipPrevious = false,
            canSeek = false,
            progress = null,
            upNext = emptyList(),
            output = null,
            collapseAtMs = null,
        ),
    )

    @Test
    fun `only the playing app's main-profile entry owns a now-playing switch`() {
        val available = nowPlayingAvailability(playing, listOf(wanda, clock, workWanda))
        assertEquals(mapOf(wanda.key to setOf(LiveRowKind.NowPlaying)), available)
        assertEquals(emptyMap<AppKey, Set<LiveRowKind>>(), nowPlayingAvailability(MediaState.Idle, listOf(wanda)))
    }

    @Test
    fun `switches show available rows and rows switched off, nothing else`() {
        val off = LiveRowRef(clock.key, LiveRowKind.Calendar)
        val toggles = liveRowToggles(
            favorites = listOf(wanda, clock),
            available = mapOf(wanda.key to setOf(LiveRowKind.NowPlaying)),
            hidden = setOf(off),
        )
        val playingSwitch = LiveRowToggle(LiveRowRef(wanda.key, LiveRowKind.NowPlaying), shown = true)
        assertEquals(listOf(playingSwitch), toggles[wanda.key])
        assertEquals(listOf(LiveRowToggle(off, shown = false)), toggles[clock.key])
    }

    @Test
    fun `now playing hides when switched off for the playing app`() {
        val off = setOf(LiveRowRef(wanda.key, LiveRowKind.NowPlaying))
        assertEquals(MediaState.Idle, playing.unlessHidden(off))
        assertSame(playing, playing.unlessHidden(setOf(LiveRowRef(clock.key, LiveRowKind.NowPlaying))))
        assertSame(playing, playing.unlessHidden(setOf(LiveRowRef(wanda.key, LiveRowKind.Calendar))))
        assertEquals(MediaState.NoAccess, MediaState.NoAccess.unlessHidden(off))
    }
}
