package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.media.MediaState
import com.wander.mono.core.media.PlayingMedia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomePlayerTest {
    private val wanda = app("com.wander.android", "Wanda")
    private val workWanda = app("com.wander.android", "Wanda", serial = 10, main = false)
    private val clock = app("com.android.deskclock", "Clock")

    private fun app(pkg: String, label: String, serial: Long = 0, main: Boolean = true) =
        LaunchableApp(AppKey(pkg, "$pkg.Main", serial), label, main)

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
        pausedAtMs = null,
    )

    @Test
    fun `the player names the main-profile app that plays`() {
        val player = homePlayer(MediaState.Active(media), listOf(workWanda, clock, wanda), shown = true)
        assertEquals(HomePlayer(media, wanda), player)
    }

    @Test
    fun `a player whose app home cannot launch still shows, without an app`() {
        assertEquals(HomePlayer(media, null), homePlayer(MediaState.Active(media), listOf(clock), shown = true))
    }

    @Test
    fun `nothing playing, no access or the player switched off shows none`() {
        assertNull(homePlayer(MediaState.Idle, listOf(wanda), shown = true))
        assertNull(homePlayer(MediaState.NoAccess, listOf(wanda), shown = true))
        assertNull(homePlayer(MediaState.Active(media), listOf(wanda), shown = false))
    }
}
