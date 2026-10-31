package com.wander.mono.data.settings

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences

/**
 * The settings a backup file carries: every user choice. Quiet mode's on/off is left out, since it
 * is a state of the moment rather than a preference. A new setting is added here too; a removed
 * one is dropped, and an older file that still carries it restores without it.
 */
val backedUpSettings: List<Setting<*>> = listOf(
    ThemeSettings.Mode,
    ThemeSettings.AmoledBlack,
    ThemeSettings.BlackWallpaper,
    ThemeSettings.Palette,
    HomeLookSettings.TextShadow,
    HomeLookSettings.Density,
    HomeLookSettings.LabelScale,
    HomeLookSettings.Reachable,
    HomeLookSettings.ListPosition,
    AccessibilitySettings.HighContrastText,
    AccessibilitySettings.SimpleMode,
    AccessibilitySettings.KeptDensity,
    AccessibilitySettings.KeptLabelScale,
    AccessibilitySettings.KeptHighContrast,
    IconSettings.Mode,
    IconSettings.Pack,
    IconSettings.Shape,
    IconSettings.ThemedTint,
    IconSettings.TintRows,
    BadgeSettings.ShowCounts,
    PreviewSettings.ShowPreviews,
    LiveActivitySettings.LiveUpdates,
    LiveActivitySettings.Timers,
    LiveActivitySettings.Navigation,
    CalendarSettings.Enabled,
    SearchSettings.Contacts,
    SearchSettings.Shortcuts,
    SearchSettings.SettingsPages,
    SearchSettings.QuickAnswers,
    SearchSettings.Notifications,
    SearchSettings.RememberRecent,
    QuietSettings.LinkDoNotDisturb,
    HiddenAppSettings.Lock,
    DrawerSettings.Background,
    DrawerSettings.RailAtStart,
    DrawerSettings.NewApps,
    DrawerSettings.Sort,
    ScreenTimeSettings.Nudge,
    GestureSettings.DoubleTapLock,
    GestureSettings.LegacySwipeDownNotifications,
    GestureSettings.SwipeUp,
    GestureSettings.SwipeDown,
    ClockSettings.Font,
    ClockSettings.Weight,
    ClockSettings.Size,
    ClockSettings.Style,
    ClockSettings.Alignment,
    ClockSettings.Date,
    ClockSettings.ShowDate,
    ClockSettings.ShowAlarm,
    ClockSettings.ShowEvent,
    ClockSettings.ShowBattery,
    MediaSettings.ShowPlayer,
    MediaSettings.ExpandedByDefault,
    MediaSettings.SwipeToSkip,
    MediaSettings.PausedHideAfter,
)

/** One setting with its value, typed together so it can be written back without a cast. */
data class SettingValue<T>(val setting: Setting<T>, val value: T) {
    /** The value as a backup file holds it. */
    val stored: Any get() = setting.toStored(value)

    fun writeTo(prefs: MutablePreferences) = setting.write(prefs, value)

    companion object {
        fun <T> of(setting: Setting<T>, prefs: Preferences) = SettingValue(setting, setting.read(prefs))

        /** [stored] read for [setting], or null when it is not of the setting's type. */
        fun <T> parse(setting: Setting<T>, stored: Any): SettingValue<T>? =
            setting.fromStored(stored)?.let { SettingValue(setting, it) }

        fun <T> default(setting: Setting<T>) = SettingValue(setting, setting.default)
    }
}
