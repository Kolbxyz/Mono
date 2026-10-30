package com.wander.mono.data.settings

object AccessibilitySettings {
    /** A stronger scrim behind home text and no dimmed text, on top of what Android's own contrast asks for. */
    val HighContrastText = BooleanSetting("high_contrast_text", default = false)

    /**
     * The large, simple preset ([simpleModeWrites]): roomy rows, larger labels, high contrast and only
     * the clock and date over the list. The three `simple_mode_kept_*` keys hold what it replaced.
     */
    val SimpleMode = BooleanSetting("simple_mode", default = false)

    val KeptDensity = EnumSetting("simple_mode_kept_density", RowDensity.Default, RowDensity.entries)

    val KeptLabelScale = IntSetting("simple_mode_kept_label_scale", default = 100, range = LABEL_SCALES)

    val KeptHighContrast = BooleanSetting("simple_mode_kept_high_contrast", default = false)
}
