package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.notifications.LiveActivity
import com.wander.mono.core.notifications.LiveActivityKind
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.core.quiet.QuietState
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Test

class QuietRowsTest {
    private val phalanx = LaunchableApp(AppKey("org.phalanx", "org.phalanx.Main", 0), "Phalanx", true)
    private val clock = LaunchableApp(AppKey("com.clock", "com.clock.Main", 0), "Clock", true)
    private val timer = live(clock, LiveActivityKind.Timer)
    private val download = live(clock, LiveActivityKind.Progress)
    private val directions = live(phalanx, LiveActivityKind.Navigation)
    private val rows = listOf(
        HomeRow.App(phalanx, AppBadge.Count(3)),
        directions,
        HomeRow.App(clock, AppBadge.Dot),
        timer,
        download,
        HomeRow.ListenerPrompt,
        CalendarRow(emptyList(), at = 0),
    )

    private fun live(app: LaunchableApp, kind: LiveActivityKind) = LiveActivityRow(
        app,
        LiveActivity(
            key = "${app.key.packageName}:$kind",
            owner = PackageUser(app.key.packageName, app.key.userSerial),
            kind = kind,
            title = "Title",
            text = null,
            progress = null,
            chronometer = null,
            icon = null,
            actions = emptyList(),
            contentIntent = null,
        ),
    )

    @Test
    fun `off leaves home as it is`() {
        assertEquals(rows, quietHomeRows(rows, QuietState(enabled = false, apps = setOf(clock.key))))
    }

    @Test
    fun `on keeps only quiet apps, without badges`() {
        val result = quietHomeRows(rows, QuietState(enabled = true, apps = setOf(phalanx.key)))
        assertEquals(listOf(HomeRow.App(phalanx, AppBadge.None), directions), result)
    }

    @Test
    fun `on keeps a kept app's timer and directions, never its progress`() {
        val result = quietHomeRows(rows, QuietState(enabled = true, apps = setOf(clock.key)))
        assertEquals(listOf(HomeRow.App(clock, AppBadge.None), timer), result)
    }

    @Test
    fun `on with no quiet apps leaves an empty list`() {
        assertEquals(emptyList<HomeRow>(), quietHomeRows(rows, QuietState(true, emptySet())))
    }
}
