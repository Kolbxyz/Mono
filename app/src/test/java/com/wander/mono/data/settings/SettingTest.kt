package com.wander.mono.data.settings

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingTest {
    @Test
    fun `every theme mode survives a write and read`() {
        ThemeMode.entries.forEach { mode ->
            val prefs = mutablePreferencesOf()
            ThemeSettings.Mode.write(prefs, mode)
            assertEquals(mode, ThemeSettings.Mode.read(prefs))
        }
    }

    @Test
    fun `theme modes are stored under their fixed names`() {
        val prefs = mutablePreferencesOf()
        ThemeSettings.Mode.write(prefs, ThemeMode.Dark)
        assertEquals("dark", prefs[stringPreferencesKey("theme_mode")])
    }

    @Test
    fun `missing or unknown stored values read as the default`() {
        assertEquals(ThemeMode.System, ThemeSettings.Mode.read(emptyPreferences()))
        assertEquals(ThemeMode.System, ThemeSettings.Mode.decode("sepia"))
        assertEquals(ThemeMode.Light, ThemeSettings.Mode.decode("light"))
    }

    @Test
    fun `booleans default until written`() {
        val prefs = mutablePreferencesOf()
        assertEquals(false, ThemeSettings.AmoledBlack.read(prefs))
        ThemeSettings.AmoledBlack.write(prefs, true)
        assertEquals(true, ThemeSettings.AmoledBlack.read(prefs))
    }
}
