package com.wander.mono.ui.edit

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.data.LiveRowKind
import com.wander.mono.data.LiveRowRef
import org.junit.Assert.assertEquals
import org.junit.Test

class LiveRowTogglesTest {
    private val wanda = app("com.wander.android", "Wanda")
    private val clock = app("com.android.deskclock", "Clock")

    private fun app(pkg: String, label: String, serial: Long = 0, main: Boolean = true) =
        LaunchableApp(AppKey(pkg, "$pkg.Main", serial), label, main)

    @Test
    fun `switches show available rows and rows switched off, nothing else`() {
        val off = LiveRowRef(clock.key, LiveRowKind.Calendar)
        val toggles = liveRowToggles(
            favorites = listOf(wanda, clock),
            available = mapOf(wanda.key to setOf(LiveRowKind.NotificationPreview)),
            hidden = setOf(off),
        )
        val previewSwitch = LiveRowToggle(LiveRowRef(wanda.key, LiveRowKind.NotificationPreview), shown = true)
        assertEquals(listOf(previewSwitch), toggles[wanda.key])
        assertEquals(listOf(LiveRowToggle(off, shown = false)), toggles[clock.key])
    }

    @Test
    fun `now playing is no live row, so no favourite has a switch for it`() {
        val toggles = liveRowToggles(listOf(wanda), available = emptyMap(), hidden = emptySet())
        assertEquals(emptyList<LiveRowToggle>(), toggles[wanda.key])
        assertEquals(false, LiveRowKind.entries.any { it.stored == "now_playing" })
    }

    @Test
    fun `switches follow the kinds' order`() {
        val toggles = liveRowToggles(
            favorites = listOf(clock),
            available = mapOf(clock.key to setOf(LiveRowKind.Timer, LiveRowKind.NotificationPreview)),
            hidden = emptySet(),
        )
        assertEquals(
            listOf(LiveRowKind.NotificationPreview, LiveRowKind.Timer),
            toggles[clock.key].orEmpty().map { it.row.kind },
        )
    }
}
