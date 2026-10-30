package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.notifications.LiveActivity
import com.wander.mono.core.notifications.LiveActivityKind
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.data.LiveRowKind
import com.wander.mono.data.LiveRowRef
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class LiveActivityRowTest {
    private val clock = app("com.android.deskclock", "Clock")
    private val maps = app("app.organicmaps", "Maps")
    private val workClock = app("com.android.deskclock", "Clock", serial = 10)

    private fun app(pkg: String, label: String, serial: Long = 0) =
        LaunchableApp(AppKey(pkg, "$pkg.Main", serial), label, serial == 0L)

    private fun activity(app: LaunchableApp, kind: LiveActivityKind) = LiveActivity(
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
    )

    private fun shape(rows: List<HomeRow>) = rows.map {
        when (it) {
            is HomeRow.App -> it.app.label + if (it.app.isMainProfile) "" else " (work)"
            is LiveActivityRow -> "live:" + it.activity.kind
            else -> "other"
        }
    }

    @Test
    fun `live rows sit right under their favourite, in kind order, in its own profile`() {
        val rows = listOf(clock, maps, workClock).map { HomeRow.App(it, AppBadge.None) }
        val live = listOf(
            activity(clock, LiveActivityKind.Timer),
            activity(clock, LiveActivityKind.Progress),
            activity(maps, LiveActivityKind.Navigation),
        )
        assertEquals(
            listOf("Clock", "live:Progress", "live:Timer", "Maps", "live:Navigation", "Clock (work)"),
            shape(withLiveActivities(rows, live)),
        )
    }

    @Test
    fun `apps that are not favourites get no live row`() {
        val rows = listOf(HomeRow.App(maps, AppBadge.None))
        assertEquals(listOf("Maps"), shape(withLiveActivities(rows, listOf(activity(clock, LiveActivityKind.Timer)))))
        assertSame(rows, withLiveActivities(rows, emptyList()))
    }

    @Test
    fun `a kind off in settings or a row switched off for that app is not shown`() {
        val timer = activity(clock, LiveActivityKind.Timer)
        val workTimer = activity(workClock, LiveActivityKind.Timer)
        val route = activity(maps, LiveActivityKind.Navigation)
        val all = listOf(timer, workTimer, route)
        val everyKind = LiveActivityKind.entries.toSet()
        assertEquals(listOf(timer, workTimer), liveActivitiesShown(all, emptySet(), setOf(LiveActivityKind.Timer)))
        val off = setOf(LiveRowRef(workClock.key, LiveRowKind.Timer), LiveRowRef(maps.key, LiveRowKind.Calendar))
        assertEquals(listOf(timer, route), liveActivitiesShown(all, off, everyKind))
    }
}
