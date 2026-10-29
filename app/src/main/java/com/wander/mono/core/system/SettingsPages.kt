package com.wander.mono.core.system

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.annotation.StringRes
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** A settings screen search can open: one of Android's by [action], or Mono's own when [action] is null. */
data class SettingsPage(val id: String, val label: String, val keywords: List<String>, val action: String?)

/**
 * The curated settings screens search offers. Only those this phone can open are listed; each
 * action is also declared in the manifest's `<queries>` so the check can see its handler.
 */
@Singleton
class SettingsPages @Inject constructor(@ApplicationContext private val context: Context) {
    private class Entry(
        val id: String,
        val action: String?,
        @StringRes val label: Int,
        @StringRes val keywords: Int,
    )

    private val entries = listOf(
        Entry("wifi", Settings.ACTION_WIFI_SETTINGS, R.string.page_wifi, R.string.page_wifi_keywords),
        Entry(
            "bluetooth",
            Settings.ACTION_BLUETOOTH_SETTINGS,
            R.string.page_bluetooth,
            R.string.page_bluetooth_keywords,
        ),
        Entry("network", Settings.ACTION_WIRELESS_SETTINGS, R.string.page_network, R.string.page_network_keywords),
        Entry("display", Settings.ACTION_DISPLAY_SETTINGS, R.string.page_display, R.string.page_display_keywords),
        Entry("battery", Intent.ACTION_POWER_USAGE_SUMMARY, R.string.page_battery, R.string.page_battery_keywords),
        Entry("sound", Settings.ACTION_SOUND_SETTINGS, R.string.page_sound, R.string.page_sound_keywords),
        Entry(
            "notifications",
            Settings.ACTION_ALL_APPS_NOTIFICATION_SETTINGS,
            R.string.page_notifications,
            R.string.page_notifications_keywords,
        ),
        Entry("apps", Settings.ACTION_APPLICATION_SETTINGS, R.string.page_apps, R.string.page_apps_keywords),
        Entry(
            "default_apps",
            Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS,
            R.string.page_default_apps,
            R.string.page_default_apps_keywords,
        ),
        Entry(
            "storage",
            Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
            R.string.page_storage,
            R.string.page_storage_keywords,
        ),
        Entry(
            "location",
            Settings.ACTION_LOCATION_SOURCE_SETTINGS,
            R.string.page_location,
            R.string.page_location_keywords,
        ),
        Entry("security", Settings.ACTION_SECURITY_SETTINGS, R.string.page_security, R.string.page_security_keywords),
        Entry("privacy", Settings.ACTION_PRIVACY_SETTINGS, R.string.page_privacy, R.string.page_privacy_keywords),
        Entry(
            "accessibility",
            Settings.ACTION_ACCESSIBILITY_SETTINGS,
            R.string.page_accessibility,
            R.string.page_accessibility_keywords,
        ),
        Entry("date", Settings.ACTION_DATE_SETTINGS, R.string.page_date, R.string.page_date_keywords),
        Entry("languages", Settings.ACTION_LOCALE_SETTINGS, R.string.page_languages, R.string.page_languages_keywords),
        Entry("about", Settings.ACTION_DEVICE_INFO_SETTINGS, R.string.page_about, R.string.page_about_keywords),
        Entry("mono", null, R.string.home_sheet_settings, R.string.page_mono_keywords),
    )

    /** Resolves every entry, so call it off the main thread; labels follow the current locale. */
    fun available(): List<SettingsPage> = entries
        .filter { it.action == null || opens(it.action) }
        .map { entry ->
            SettingsPage(
                id = entry.id,
                label = context.getString(entry.label),
                keywords = context.getString(entry.keywords).split(',').map { it.trim() }.filter { it.isNotEmpty() },
                action = entry.action,
            )
        }

    /** Opens one of Android's pages; Mono's own (null action) is opened by the caller. */
    fun open(page: SettingsPage): ActionResult {
        val action = page.action ?: return ActionResult.Failed(R.string.error_settings_missing)
        return try {
            context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            ActionResult.Done
        } catch (_: ActivityNotFoundException) {
            ActionResult.Failed(R.string.error_settings_missing)
        }
    }

    private fun opens(action: String): Boolean =
        context.packageManager.resolveActivity(Intent(action), PackageManager.MATCH_DEFAULT_ONLY) != null
}
