package com.wander.mono.data.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePresetsTest {
    @Test
    fun `every preset reads back as itself once written`() {
        ThemePreset.entries.forEach { preset ->
            val prefs = mutablePreferencesOf()
            presetWrites(preset.look, keepDensity = false).forEach { it.writeTo(prefs) }
            assertEquals(preset.name, preset.look, lookIn(prefs))
        }
    }

    @Test
    fun `with the simple preset on, row density is left alone`() {
        val prefs = mutablePreferencesOf()
        HomeLookSettings.Density.write(prefs, RowDensity.Comfortable)
        presetWrites(ThemePreset.Mono.look, keepDensity = true).forEach { it.writeTo(prefs) }
        assertEquals(RowDensity.Comfortable, HomeLookSettings.Density.read(prefs))
        assertEquals(ThemePalette.Graphite, ThemeSettings.Palette.read(prefs))
    }

    @Test
    fun `classic is the untouched defaults, so a new install shows it chosen`() {
        assertEquals(ThemePreset.Classic.look, lookIn(mutablePreferencesOf()))
    }

    @Test
    fun `each preset's clock weight is one its font has`() {
        ThemePreset.entries.forEach { preset ->
            assertTrue(preset.name, preset.look.clock.weight in preset.look.clock.font.weights)
        }
    }

    @Test
    fun `the palette is backed up and wallpaper is the default`() {
        assertTrue(ThemeSettings.Palette in backedUpSettings)
        assertEquals(ThemePalette.Wallpaper, ThemeSettings.Palette.decode(null))
        ThemePalette.entries.forEach { palette ->
            val prefs = mutablePreferencesOf()
            ThemeSettings.Palette.write(prefs, palette)
            assertEquals(palette, ThemeSettings.Palette.read(prefs))
        }
    }

    private fun lookIn(prefs: Preferences) = PresetLook(
        palette = ThemeSettings.Palette.read(prefs),
        clock = PresetClock(
            ClockSettings.Font.read(prefs),
            ClockSettings.Weight.read(prefs),
            ClockSettings.Style.read(prefs),
        ),
        iconShape = IconSettings.Shape.read(prefs),
        iconMode = IconSettings.Mode.read(prefs),
        drawer = DrawerSettings.Background.read(prefs),
        textShadow = HomeLookSettings.TextShadow.read(prefs),
        density = HomeLookSettings.Density.read(prefs),
    )
}
