package com.wander.mono.core.widgets

import com.wander.mono.core.apps.AppKey
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetLayoutTest {
    private val wanda = key("com.wander.android")
    private val phalanx = key("org.phalanx.android")
    private val clock = key("com.android.deskclock")
    private val favorites = listOf(wanda, phalanx)

    private fun key(pkg: String, serial: Long = 0) = AppKey(pkg, "$pkg.Main", serial)

    private fun widget(id: Int, anchor: AppKey?) = WidgetPlacement(id, "p/.W$id", 0, anchor, 112)

    private fun order(placements: List<WidgetPlacement>) = homeSlots(favorites, placements).map {
        when (it) {
            is HomeSlot.Favorite -> it.key.packageName
            is HomeSlot.Widget -> "w${it.placement.appWidgetId}"
        }
    }

    @Test
    fun widgetsSitUnderTheirFavourite() {
        val placements = listOf(widget(1, phalanx), widget(2, null), widget(3, wanda))
        assertEquals(listOf("w2", "com.wander.android", "w3", "org.phalanx.android", "w1"), order(placements))
    }

    @Test
    fun widgetOfRemovedFavouriteGoesUnderTheLast() {
        assertEquals(listOf("com.wander.android", "org.phalanx.android", "w1"), order(listOf(widget(1, clock))))
    }

    @Test
    fun withoutFavouritesEverythingIsOnTop() {
        val layout = layOutWidgets(emptyList(), listOf(widget(1, clock), widget(2, null))) { it.anchor }
        assertEquals(listOf(1, 2), layout.top.map { it.appWidgetId })
    }

    @Test
    fun movingUpPastAFavouriteReanchors() {
        val moved = moveWidget(favorites, listOf(widget(1, phalanx)), appWidgetId = 1, by = -1)
        assertEquals(listOf(widget(1, wanda)), moved)
        val top = moveWidget(favorites, moved, appWidgetId = 1, by = -1)
        assertEquals(listOf(widget(1, null)), top)
    }

    @Test
    fun movingPastAnotherWidgetSwapsOrder() {
        val placements = listOf(widget(1, wanda), widget(2, wanda))
        assertEquals(listOf(widget(2, wanda), widget(1, wanda)), moveWidget(favorites, placements, 2, -1))
    }

    @Test
    fun movingPastTheEndChangesNothing() {
        val placements = listOf(widget(1, phalanx))
        assertEquals(placements, moveWidget(favorites, placements, 1, 1))
        assertEquals(placements, moveWidget(favorites, placements, 99, -1))
    }

    @Test
    fun newWidgetGoesUnderItsOwnApp() {
        assertEquals(wanda, defaultAnchor(favorites, "com.wander.android", 0))
        assertEquals(phalanx, defaultAnchor(favorites, "com.wander.android", 10))
        assertEquals(null, defaultAnchor(emptyList(), "com.wander.android", 0))
    }
}
