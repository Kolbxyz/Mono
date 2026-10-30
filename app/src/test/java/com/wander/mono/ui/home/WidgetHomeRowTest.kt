package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.core.widgets.PlacedWidget
import com.wander.mono.core.widgets.WidgetPlacement
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetHomeRowTest {
    private val wanda = app("com.wander.android")
    private val phalanx = app("org.phalanx.android")
    private val playing = HomeRow.NowPlaying(
        PlayingMedia(
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
        ),
    )

    private fun app(pkg: String) = HomeRow.App(LaunchableApp(AppKey(pkg, "$pkg.Main", 0), pkg, true), AppBadge.None)

    private fun widget(id: Int, anchor: HomeRow.App?) =
        PlacedWidget(WidgetPlacement(id, "p/.W", 0, anchor?.app?.key, 112), "W$id", canReconfigure = false)

    private fun keys(rows: List<HomeRow>) = rows.map { it.key }

    @Test
    fun noWidgetsLeavesRowsAlone() {
        val rows = listOf(wanda, playing, phalanx)
        assertEquals(rows, withWidgetRows(rows, emptyList()))
    }

    @Test
    fun widgetGoesAfterItsFavouritesNowPlaying() {
        val prompt = HomeRow.ListenerPrompt
        val rows = withWidgetRows(listOf(wanda, playing, phalanx, prompt), listOf(widget(1, wanda)))
        assertEquals(keys(listOf(wanda, playing)) + "widget:1" + keys(listOf(phalanx, prompt)), keys(rows))
    }

    @Test
    fun topAndLastFavouriteWidgets() {
        val widgets = listOf(widget(1, phalanx), widget(2, null))
        val rows = withWidgetRows(listOf(wanda, phalanx, HomeRow.ListenerPrompt), widgets)
        assertEquals(
            listOf("widget:2") + keys(listOf(wanda, phalanx)) + "widget:1" + HomeRow.ListenerPrompt.key,
            keys(rows),
        )
    }

    @Test
    fun widgetGoesAfterEveryLiveRowOfItsFavourite() {
        val calendar = CalendarRow(emptyList(), at = 0)
        val rows = withWidgetRows(listOf(wanda, playing, calendar, phalanx), listOf(widget(1, wanda)))
        assertEquals(keys(listOf(wanda, playing, calendar)) + "widget:1" + phalanx.key, keys(rows))
    }
}
