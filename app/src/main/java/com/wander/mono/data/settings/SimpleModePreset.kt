package com.wander.mono.data.settings

/** What the preset sets: the roomiest rows, labels a third larger, high-contrast text. */
val SimplePreset = SimpleLook(RowDensity.Comfortable, labelScale = 130, highContrast = true)

/**
 * The writes, for one edit, that switch the preset to [on]. Turning it on keeps [current] (what
 * home uses now) in the `kept` keys before setting [SimplePreset]; turning it off puts [kept] back.
 * Nothing when it already is [on], so a repeated tap never overwrites what was kept with the preset.
 */
fun simpleModeWrites(on: Boolean, wasOn: Boolean, current: SimpleLook, kept: SimpleLook): List<SettingValue<*>> {
    if (on == wasOn) return emptyList()
    val applied = if (on) SimplePreset else kept
    val keep = if (on) {
        listOf(
            SettingValue(AccessibilitySettings.KeptDensity, current.density),
            SettingValue(AccessibilitySettings.KeptLabelScale, current.labelScale),
            SettingValue(AccessibilitySettings.KeptHighContrast, current.highContrast),
        )
    } else {
        emptyList()
    }
    return keep + listOf(
        SettingValue(HomeLookSettings.Density, applied.density),
        SettingValue(HomeLookSettings.LabelScale, applied.labelScale),
        SettingValue(AccessibilitySettings.HighContrastText, applied.highContrast),
        SettingValue(AccessibilitySettings.SimpleMode, on),
    )
}
