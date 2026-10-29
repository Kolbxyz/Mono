package com.wander.mono.data.settings

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValueSettingsTest {
    @Test
    fun `numbers read clamped to their range`() {
        val prefs = mutablePreferencesOf(intPreferencesKey("clock_size") to 400)
        assertEquals(CLOCK_SIZES.last, ClockSettings.Size.read(prefs))
        assertEquals(ClockSettings.Size.default, ClockSettings.Size.read(emptyPreferences()))
    }

    @Test
    fun `numbers from a backup are whole and in range`() {
        assertEquals(96, ClockSettings.Size.fromStored(96))
        assertEquals(CLOCK_SIZES.first, ClockSettings.Size.fromStored(2L))
        assertNull(ClockSettings.Size.fromStored("96"))
        assertNull(ClockSettings.Size.fromStored(96.5))
    }

    @Test
    fun `the icon pack defaults to the apps' own icons`() {
        assertEquals(SYSTEM_ICON_PACK, IconSettings.Pack.read(emptyPreferences()))
        val prefs = mutablePreferencesOf()
        IconSettings.Pack.write(prefs, "org.example.pack")
        assertEquals("org.example.pack", IconSettings.Pack.read(prefs))
    }
}
