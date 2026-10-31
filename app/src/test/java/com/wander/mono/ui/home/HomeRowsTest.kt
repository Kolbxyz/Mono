package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.ui.components.AppBadge
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

    private fun rows(
        favorites: List<AppKey>,
        listenerMissing: Boolean = false,
        badges: Map<PackageUser, Int> = emptyMap(),
        showCounts: Boolean = true,
    ) = assembleHomeRows(favorites, catalog, badges, listenerMissing, showCounts)

    private fun shape(rows: List<HomeRow>) = rows.map {
        when (it) {
            is HomeRow.App -> it.app.label + if (it.app.isMainProfile) "" else " (work)"
            HomeRow.ListenerPrompt -> "prompt"
            is NotificationRow -> "notification:" + it.app.label
            is CalendarRow -> "calendar"
            is LiveActivityRow -> "live"
            is WidgetHomeRow -> "widget:" + it.widget.placement.appWidgetId
            is HomeRow.Folder -> "folder:" + it.name
            is DigestRow -> "digest"
            is FocusRow -> "focus"
        }
    }

    @Test
    fun `favourites keep the user's order and skip apps that are not installed`() {
        val missing = AppKey("gone.app", "gone.app.Main", 0)
        assertEquals(listOf("Clock", "Phalanx"), shape(rows(listOf(clock.key, missing, phalanx.key))))
    }

    @Test
    fun `work profile favourites keep their own row and duplicates show once`() {
        val favorites = listOf(workWanda.key, phalanx.key, wanda.key, phalanx.key)
        assertEquals(listOf("Wanda (work)", "Phalanx", "Wanda"), shape(rows(favorites)))
    }

    @Test
    fun `missing notification access adds one prompt row at the end`() {
        val result = rows(listOf(phalanx.key, clock.key), listenerMissing = true)
        assertEquals(listOf("Phalanx", "Clock", "prompt"), shape(result))
    }

    @Test
    fun `badges are per app and per profile`() {
        val badges = mapOf(PackageUser("com.wander.android", 10) to 2)
        val result = rows(listOf(wanda.key, workWanda.key), badges = badges)
        assertEquals(listOf(AppBadge.None, AppBadge.Count(2)), result.map { (it as HomeRow.App).badge })
    }

    @Test
    fun `badges fall back to dots when counts are off`() {
        val badges = mapOf(PackageUser("org.phalanx.android", 0) to 3)
        val result = rows(listOf(phalanx.key), badges = badges, showCounts = false)
        assertEquals(listOf(AppBadge.Dot), result.map { (it as HomeRow.App).badge })
    }
}
