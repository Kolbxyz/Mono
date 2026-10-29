package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.core.quiet.QuietState
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Test

class QuietRowsTest {
    private val phalanx = LaunchableApp(AppKey("org.phalanx", "org.phalanx.Main", 0), "Phalanx", true)
    private val clock = LaunchableApp(AppKey("com.clock", "com.clock.Main", 0), "Clock", true)
    private val media = PlayingMedia(
        "com.wander.android",
        "Song",
        "Artist",
        art = null,
        artColor = null,
        isPlaying = true,
        canSkipNext = true,
        canSkipPrevious = true,
        canSeek = true,
        progress = null,
        upNext = emptyList(),
        output = null,
    )
    private val rows = listOf(
        HomeRow.App(phalanx, AppBadge.Count(3)),
        HomeRow.NowPlaying(media),
        HomeRow.App(clock, AppBadge.Dot),
        HomeRow.ListenerPrompt,
        CalendarRow(emptyList(), at = 0),
    )

    @Test
    fun `off leaves home as it is`() {
        assertEquals(rows, quietHomeRows(rows, QuietState(enabled = false, apps = setOf(clock.key))))
    }

    @Test
    fun `on keeps only quiet apps, without badges, and the playing media`() {
        val result = quietHomeRows(rows, QuietState(enabled = true, apps = setOf(phalanx.key)))
        assertEquals(listOf(HomeRow.App(phalanx, AppBadge.None), HomeRow.NowPlaying(media)), result)
    }

    @Test
    fun `on with no quiet apps leaves only the playing media`() {
        assertEquals(listOf(HomeRow.NowPlaying(media)), quietHomeRows(rows, QuietState(true, emptySet())))
    }
}
