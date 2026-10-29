package com.wander.mono.core.widgets

import com.wander.mono.core.apps.AppKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetPlacementTest {
    private val anchor = AppKey("com.wander.android", "com.wander.android.Main", 0)

    @Test
    fun roundTripsWithAndWithoutAnchor() {
        val placed = WidgetPlacement(42, "com.example/com.example.ClockWidget", 10, anchor, 184)
        assertEquals(placed, WidgetPlacement.decode(placed.encode()))
        val top = placed.copy(anchor = null)
        assertEquals(top, WidgetPlacement.decode(top.encode()))
    }

    @Test
    fun rejectsMalformedLines() {
        assertNull(WidgetPlacement.decode(""))
        assertNull(WidgetPlacement.decode("x|com.example/.W|0||184"))
        assertNull(WidgetPlacement.decode("1||0||184"))
        assertNull(WidgetPlacement.decode("1|com.example/.W|0|not-a-key|184"))
        assertNull(WidgetPlacement.decode("1|com.example/.W|0||"))
    }

    @Test
    fun sizeFitsMinimumHeight() {
        assertEquals(WidgetSize.Small, WidgetSize.fitting(40))
        assertEquals(WidgetSize.Medium, WidgetSize.fitting(150))
        assertEquals(WidgetSize.Large, WidgetSize.fitting(500))
    }

    @Test
    fun nearestSizeOfStoredHeight() {
        assertEquals(WidgetSize.Small, WidgetSize.nearest(100))
        assertEquals(WidgetSize.Medium, WidgetSize.nearest(200))
        assertEquals(WidgetSize.Large, WidgetSize.nearest(400))
    }
}
