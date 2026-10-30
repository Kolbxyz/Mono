package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable

/** A settings section: a group of rows, from its own `*Section.kt` file. */
internal typealias SettingsSection = @Composable (SettingsViewModel) -> Unit

/**
 * Each hub page's sections, top to bottom. A feature adds its section with one line in the page
 * it belongs to; the section itself lives in its own `*Section.kt` file.
 */
internal fun sectionsOf(category: SettingsCategory): List<SettingsSection> = when (category) {
    SettingsCategory.HomeClock -> listOf(
        { ClockPreviewSection() },
        { ClockSection(it) },
        { GlanceLinesSection(it) },
        { HomeLookSection(it) },
        { HeadphoneSection(it) },
        { CalendarSection() },
    )
    SettingsCategory.DrawerSearch -> listOf(
        { DrawerSection(it) },
        { SearchSection(it) },
    )
    SettingsCategory.Appearance -> listOf(
        { ThemeSection(it) },
        { IconSection(it) },
        { AccessibilitySection(it) },
    )
    SettingsCategory.Notifications -> listOf(
        { BadgeSection(it) },
        { PreviewSection(it) },
        { LiveActivitySection(it) },
        { QuietSection() },
    )
    SettingsCategory.Gestures -> listOf(
        { GestureSection(it) },
    )
    SettingsCategory.PrivacyAbout -> listOf(
        { PrivacySection() },
        { HiddenAppsSection() },
        { UsageSection(it) },
        { BackupSection() },
        { AboutSection() },
    )
}
