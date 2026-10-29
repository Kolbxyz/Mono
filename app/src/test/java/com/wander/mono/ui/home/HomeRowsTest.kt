package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.media.MediaState
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.core.notifications.PackageUser
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeRowsTest {
    private val wanda = app("com.wander.android", "Wanda")
    private val phalanx = app("org.phalanx.android", "Phalanx")
    private val clock = app("com.android.deskclock", "Clock")
    private val workWanda = app("com.wander.android", "Wanda", serial = 10, main = false)
    private val catalog = listOf(wanda, phalanx, clock, workWanda)

    private fun app(pkg: String, label: String, serial: Long = 0, main: Boolean = true) =
        LaunchableApp(AppKey(pkg, "$pkg.Main", serial), label, main)

    private fun playing(pkg: String) = MediaState.Active(
        PlayingMedia(pkg, "Song", "Artist", art = null, artColor = null, isPlaying = true, canSkipNext = true),
    )

    private fun shape(rows: List<HomeRow>) = rows.map {
        when (it) {
            is HomeRow.App -> it.app.label + if (it.app.isMainProfile) "" else " (work)"
            is HomeRow.NowPlaying -> "now:" + it.media.packageName
            HomeRow.ListenerPrompt -> "prompt"
        }
    }

    @Test
    fun `favourites keep the user's order and skip apps that are not installed`() {
        val missing = AppKey("gone.app", "gone.app.Main", 0)
        val rows = assembleHomeRows(listOf(clock.key, missing, phalanx.key), catalog, emptyMap(), MediaState.Idle)
        assertEquals(listOf("Clock", "Phalanx"), shape(rows))
    }

    @Test
    fun `now playing sits right under the main-profile favourite that owns the session`() {
        val favorites = listOf(workWanda.key, phalanx.key, wanda.key, clock.key)
        val rows = assembleHomeRows(favorites, catalog, emptyMap(), playing("com.wander.android"))
        assertEquals(listOf("Wanda (work)", "Phalanx", "Wanda", "now:com.wander.android", "Clock"), shape(rows))
    }

    @Test
    fun `now playing goes first when its app is not a favourite`() {
        val rows = assembleHomeRows(listOf(phalanx.key), catalog, emptyMap(), playing("com.wander.android"))
        assertEquals(listOf("now:com.wander.android", "Phalanx"), shape(rows))
    }

    @Test
    fun `missing notification access adds one prompt row at the end`() {
        val rows = assembleHomeRows(listOf(phalanx.key, clock.key), catalog, emptyMap(), MediaState.NoAccess)
        assertEquals(listOf("Phalanx", "Clock", "prompt"), shape(rows))
    }

    @Test
    fun `dots are per app and per profile`() {
        val badges = mapOf(PackageUser("com.wander.android", 10) to 2)
        val rows = assembleHomeRows(listOf(wanda.key, workWanda.key), catalog, badges, MediaState.Idle)
        assertEquals(listOf(false, true), rows.map { (it as HomeRow.App).hasNotifications })
    }
}
