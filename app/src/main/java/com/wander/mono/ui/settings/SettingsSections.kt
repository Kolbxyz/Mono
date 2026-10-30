package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable

/** A settings section: a group of rows, from its own `*Section.kt` file. */
internal typealias SettingsSection = @Composable (SettingsViewModel) -> Unit

/**
 * Each hub page's sections, top to bottom. A feature adds its section with one line in the page
 * it belongs to; the section itself lives in its own `*Section.kt` file.
 */
internal fun sectionsOf(category: SettingsCategory): List<SettingsSection> = when (category) {
    SettingsCategory.Home -> listOf(
        { ClockPreviewSection() },
        { ClockSection(it) },
        { GlanceLinesSection(it) },
        { CalendarSection() },
        { HomeLookSection(it) },
    )
    SettingsCategory.Appearance -> listOf(
        { ThemeSection(it) },
        { IconSection(it) },
        { AccessibilitySection(it) },
    )
    SettingsCategory.DrawerSearch -> listOf(
        { DrawerSection(it) },
        { SearchSection(it) },
    )
    SettingsCategory.Notifications -> listOf(
        { NotificationSection(it) },
        { LiveActivitySection(it) },
    )
    SettingsCategory.Gestures -> listOf(
        { GestureSection(it) },
    )
    SettingsCategory.Focus -> listOf(
        { QuietSection() },
        { RoutinesSection(it) },
        { HeadphoneSection(it) },
        { ScreenTimeSection(it) },
    )
    SettingsCategory.Privacy -> listOf(
        { PrivacySection() },
        { HiddenAppsSection() },
        { BackupSection() },
    )
    SettingsCategory.About -> listOf(
        { AboutSection() },
    )
}
