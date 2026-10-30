package com.wander.mono.data.settings

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences

/**
 * The settings a backup file carries: every user choice. Quiet mode's on/off is left out, since it
 * is a state of the moment rather than a preference. A new setting is added here too.
 */
val backedUpSettings: List<Setting<*>> = listOf(
    ThemeSettings.Mode,
    ThemeSettings.AmoledBlack,
    ThemeSettings.BlackWallpaper,
    HomeLookSettings.TextShadow,
    HomeLookSettings.Density,
    HomeLookSettings.LabelScale,
    HomeLookSettings.Reachable,
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
    QuietSettings.LinkDoNotDisturb,
    HiddenAppSettings.Lock,
    UsageSettings.ShowInSheet,
    DrawerSettings.Background,
    DrawerSettings.KeyboardOnOpen,
    DrawerSettings.RailAtStart,
    HeadphoneSettings.LiftApps,
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
