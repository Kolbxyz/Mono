package com.wander.mono.data.settings

import androidx.datastore.preferences.core.mutablePreferencesOf
import org.junit.Assert.assertEquals
import org.junit.Test

class IconSettingsTest {
    @Test
    fun `every icon mode survives a write and read`() {
        IconMode.entries.forEach { mode ->
            val prefs = mutablePreferencesOf()
            IconSettings.Mode.write(prefs, mode)
            assertEquals(mode, IconSettings.Mode.read(prefs))
        }
    }

    @Test
    fun `full colour is the default and the fallback for unknown values`() {
        assertEquals(IconMode.Colour, IconSettings.Mode.decode(null))
        assertEquals(IconMode.Colour, IconSettings.Mode.decode("TextOnly"))
        assertEquals(IconMode.TextOnly, IconSettings.Mode.decode("text_only"))
    }

    @Test
    fun `every icon shape survives a write and read and the system shape is the default`() {
        IconShape.entries.forEach { shape ->
            val prefs = mutablePreferencesOf()
            IconSettings.Shape.write(prefs, shape)
            assertEquals(shape, IconSettings.Shape.read(prefs))
        }
        assertEquals(IconShape.System, IconSettings.Shape.decode(null))
        assertEquals(IconShape.System, IconSettings.Shape.decode("blob"))
    }
}
