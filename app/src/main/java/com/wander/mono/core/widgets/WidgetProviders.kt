package com.wander.mono.core.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.UserHandle
import com.wander.mono.core.apps.Profiles
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.Collator
import javax.inject.Inject
import javax.inject.Singleton

/** One app's widgets in the picker. */
data class WidgetApp(
    val packageName: String,
    val label: String,
    val isMainProfile: Boolean,
    /** Its profile: one app may offer widgets from several (work, clone). */
    val profile: UserHandle,
    val widgets: List<WidgetChoice>,
)

data class WidgetChoice(val info: AppWidgetProviderInfo, val label: String)

/** The widgets installed apps offer for home, in every profile, grouped by app and sorted by name. */
@Singleton
class WidgetProviders @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profiles: Profiles,
) {
    private val manager = AppWidgetManager.getInstance(context)
    private val launcherApps = context.getSystemService(LauncherApps::class.java)

    /** Reads every provider's label: call it off the main thread. */
    fun load(): List<WidgetApp> {
        val pm = context.packageManager
        val collator = Collator.getInstance()
        // No widgets from the private space: its apps never go on home.
        return launcherApps.profiles
            .filterNot(profiles::isPrivate)
            .flatMap { user -> manager.getInstalledProvidersForProfile(user) }
            .filter { it.widgetCategory and AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN != 0 }
            .groupBy { it.provider.packageName to it.profile }
            .mapNotNull { (owner, infos) ->
                val (packageName, user) = owner
                // Null when the app went away since the providers were listed.
                val app = try {
                    launcherApps.getApplicationInfo(packageName, 0, user)
                } catch (_: PackageManager.NameNotFoundException) {
                    return@mapNotNull null
                }
                WidgetApp(
                    packageName = packageName,
                    label = app.loadLabel(pm).toString(),
                    isMainProfile = user == profiles.main,
                    profile = user,
                    widgets = infos
                        .map { WidgetChoice(it, it.loadLabel(pm)) }
                        .sortedWith(compareBy(collator) { it.label }),
                )
            }
            .sortedWith(compareBy<WidgetApp, String>(collator) { it.label }.thenBy { !it.isMainProfile })
    }
}
