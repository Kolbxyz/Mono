package com.wander.mono.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleModePresetTest {
    private val mine = SimpleLook(RowDensity.Compact, labelScale = 110, highContrast = false)
    private val untouched = SimpleLook(RowDensity.Default, labelScale = 100, highContrast = false)

    @Test
    fun `turning it on keeps the current look, then applies the preset`() {
        val writes = simpleModeWrites(on = true, wasOn = false, current = mine, kept = untouched)
        assertEquals(
            listOf(
                SettingValue(AccessibilitySettings.KeptDensity, RowDensity.Compact),
                SettingValue(AccessibilitySettings.KeptLabelScale, 110),
                SettingValue(AccessibilitySettings.KeptHighContrast, false),
                SettingValue(HomeLookSettings.Density, SimplePreset.density),
                SettingValue(HomeLookSettings.LabelScale, SimplePreset.labelScale),
                SettingValue(AccessibilitySettings.HighContrastText, true),
                SettingValue(AccessibilitySettings.SimpleMode, true),
            ),
            writes,
        )
    }

    @Test
    fun `turning it off puts the kept look back`() {
        val writes = simpleModeWrites(on = false, wasOn = true, current = SimplePreset, kept = mine)
        assertEquals(
            listOf(
                SettingValue(HomeLookSettings.Density, RowDensity.Compact),
                SettingValue(HomeLookSettings.LabelScale, 110),
                SettingValue(AccessibilitySettings.HighContrastText, false),
                SettingValue(AccessibilitySettings.SimpleMode, false),
            ),
            writes,
        )
    }

    @Test
    fun `a repeated switch writes nothing, so the kept look survives`() {
        assertTrue(simpleModeWrites(on = true, wasOn = true, current = SimplePreset, kept = mine).isEmpty())
        assertTrue(simpleModeWrites(on = false, wasOn = false, current = mine, kept = untouched).isEmpty())
    }

    @Test
    fun `the preset stays inside the label scale range`() {
        assertTrue(SimplePreset.labelScale in HomeLookSettings.LabelScale.range)
    }
}
