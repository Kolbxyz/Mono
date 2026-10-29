package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable

/**
 * The settings screen's sections, top to bottom. A feature adds its section with one line here;
 * the section itself lives in its own `*Section.kt` file.
 */
internal val settingsSections: List<@Composable (SettingsViewModel) -> Unit> = listOf(
    { ThemeSection(it) },
    { HomeLookSection(it) },
    { IconSection(it) },
    { BadgeSection(it) },
    { PreviewSection(it) },
    { CalendarSection() },
    { SearchSection(it) },
    { QuietSection(it) },
    { HiddenAppsSection(it) },
    { UsageSection(it) },
)
