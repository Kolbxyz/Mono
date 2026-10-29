package com.wander.mono.data.settings

object AccessibilitySettings {
    /** A stronger scrim behind home text and no dimmed text, on top of what Android's own contrast asks for. */
    val HighContrastText = BooleanSetting("high_contrast_text", default = false)
}
