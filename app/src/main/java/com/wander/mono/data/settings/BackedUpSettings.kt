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
    HomeLookSettings.TextShadow,
    AccessibilitySettings.HighContrastText,
    IconSettings.Mode,
    IconSettings.Pack,
    BadgeSettings.ShowCounts,
    PreviewSettings.ShowPreviews,
    CalendarSettings.Enabled,
    SearchSettings.Contacts,
    QuietSettings.LinkDoNotDisturb,
    HiddenAppSettings.Lock,
    UsageSettings.ShowInSheet,
    DrawerSettings.Background,
    HeadphoneSettings.LiftApps,
    GestureSettings.DoubleTapLock,
    GestureSettings.SwipeDownNotifications,
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
