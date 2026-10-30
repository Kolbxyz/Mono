package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.widgets.PlacedWidget
import com.wander.mono.core.widgets.WidgetPlacement
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Test

class LiftedRowsTest {
    private val phalanx = app("org.phalanx.android")
    private val clock = app("com.clock")
    private val wanda = app("com.wander.android")
    private val podcasts = app("org.podcasts")
    private val calendar = CalendarRow(emptyList(), at = 0)

    private fun app(pkg: String) = HomeRow.App(LaunchableApp(AppKey(pkg, "$pkg.Main", 0), pkg, true), AppBadge.None)

    private fun widget(id: Int) =
        WidgetHomeRow(PlacedWidget(WidgetPlacement(id, "p/.W", 0, null, 112), "W$id", canReconfigure = false))

    private fun keys(rows: List<HomeRow>) = rows.map { it.key }

    @Test
    fun nothingLiftedLeavesHomeAsItIs() {
        val rows = listOf(phalanx, clock, wanda)
        assertEquals(rows, liftedFirst(rows, emptySet()))
    }

    @Test
    fun liftedAppsGoFirstWithTheirLiveRows() {
        val wandaWidget = widget(1)
        val rows = listOf(phalanx, clock, wanda, calendar, wandaWidget, podcasts)
        val result = liftedFirst(rows, setOf(wanda.app.key))
        assertEquals(keys(listOf(wanda, calendar, wandaWidget, phalanx, clock, podcasts)), keys(result))
    }

    @Test
    fun liftedAppsKeepTheUsersOrderAmongThemselves() {
        val rows = listOf(phalanx, podcasts, clock, wanda)
        val result = liftedFirst(rows, setOf(wanda.app.key, podcasts.app.key))
        assertEquals(keys(listOf(podcasts, wanda, phalanx, clock)), keys(result))
    }

    @Test
    fun rowsAboveTheFirstFavouriteStayFirstAndThePromptStaysLast() {
        val topWidget = widget(2)
        val rows = listOf(topWidget, calendar, phalanx, wanda, HomeRow.ListenerPrompt)
        val result = liftedFirst(rows, setOf(wanda.app.key))
        assertEquals(keys(listOf(topWidget, calendar, wanda, phalanx, HomeRow.ListenerPrompt)), keys(result))
    }

    @Test
    fun liftedKeysNotOnHomeChangeNothing() {
        val rows = listOf(phalanx, clock)
        assertEquals(rows, liftedFirst(rows, setOf(wanda.app.key)))
    }
}
