package com.wander.mono.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GestureSettingsTest {
    @Test
    fun `old swipe down switch off carries over as nothing`() {
        assertEquals(SwipeDownAction.None, swipeDownFromLegacy(notificationsOn = false))
    }

    @Test
    fun `old swipe down switch on leaves the new choice alone`() {
        assertNull(swipeDownFromLegacy(notificationsOn = true))
    }

    @Test
    fun `new gesture choices keep their stored names`() {
        assertEquals(listOf("drawer", "search"), SwipeUpAction.entries.map { it.stored })
        assertEquals(
            listOf("notifications", "quick_settings", "search", "none"),
            SwipeDownAction.entries.map { it.stored },
        )
        assertEquals(SwipeDownAction.Notifications, GestureSettings.SwipeDown.decode(null))
    }
}
