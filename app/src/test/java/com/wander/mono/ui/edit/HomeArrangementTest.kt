package com.wander.mono.ui.edit

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.widgets.WidgetPlacement
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeArrangementTest {
    private val mail = AppKey("com.mail", "Main", 0)
    private val music = AppKey("com.music", "Main", 0)

    private fun widget(id: Int, anchor: AppKey?) = WidgetPlacement(id, "com.w/.W$id", 0, anchor, 184)

    @Test
    fun `re-anchors widgets and puts them in the dragged order`() {
        val placements = listOf(widget(1, mail), widget(2, mail))
        val arranged = placements.arranged(listOf(WidgetSpot(2, null), WidgetSpot(1, music)))
        assertEquals(listOf(widget(2, null), widget(1, music)), arranged)
    }

    @Test
    fun `widgets not shown keep their anchor and follow`() {
        val placements = listOf(widget(1, mail), widget(2, music))
        val arranged = placements.arranged(listOf(WidgetSpot(2, null)))
        assertEquals(listOf(widget(2, null), widget(1, mail)), arranged)
    }

    @Test
    fun `spots of widgets no longer placed are ignored`() {
        val placements = listOf(widget(1, mail))
        assertEquals(placements, placements.arranged(listOf(WidgetSpot(9, null), WidgetSpot(1, mail))))
    }
}
