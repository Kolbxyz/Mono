package com.wander.mono.core.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.os.Build
import com.wander.mono.data.CustomLabels
import com.wander.mono.di.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.Collator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * Every launchable activity in every profile, under the user's names where they renamed one,
 * sorted by label, reloaded on each [CatalogChange].
 */
@Singleton
class AppCatalog @Inject constructor(
    @ApplicationContext context: Context,
    private val profiles: Profiles,
    private val icons: IconCache,
    events: CatalogEvents,
    customLabels: CustomLabels,
    @AppScope scope: CoroutineScope,
) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val packageManager = context.packageManager
    private val collator = Collator.getInstance()

    /** Alphabetical; apps sharing a label follow the name that tells them apart. */
    private val byLabel = compareBy<LaunchableApp, String>(collator) { it.label }.thenBy { it.detail }

    private val installed: StateFlow<List<LaunchableApp>?> = events.changes
        .filter { it !is CatalogChange.Shortcuts }
        .onEach { change ->
            if (change is CatalogChange.Packages) icons.evict(change.packageNames) else icons.evictAll()
        }
        .onStart { emit(CatalogChange.Profiles) }
        .conflate()
        .map { load() }
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, null)

    /** Null until the first load finishes, so callers can tell "loading" from "no apps". */
    val apps: StateFlow<List<LaunchableApp>?> = combine(installed, customLabels.labels) { loaded, labels ->
        loaded?.let { withCustomLabels(it, labels).sortedWith(byLabel) }
    }.flowOn(Dispatchers.Default).stateIn(scope, SharingStarted.Eagerly, null)

    /**
     * A locked private space lists nothing: its apps are neither shown nor searchable until it is
     * unlocked. Install times are read here, once per package and load, never on a timer.
     */
    private fun load(): List<LaunchableApp> = profiles.listed().flatMap { user ->
        val serial = profiles.serialOf(user)
        val isMain = user == profiles.main
        val isPrivate = profiles.isPrivate(user)
        val installTimes = mutableMapOf<String, Long?>()
        launcherApps.getActivityList(null, user).map { info ->
            val component = info.componentName
            val key = AppKey(component.packageName, component.className, serial)
            val appInfo = info.applicationInfo
            val archived = isArchived(appInfo)
            val installedAt = if (isMain && !archived) {
                installTimes.getOrPut(component.packageName) { firstInstallTime(packageManager, component.packageName) }
            } else {
                null
            }
            val app = LaunchableApp(
                key,
                info.label.toString(),
                isMain,
                isPrivate = isPrivate,
                category = appCategory(appInfo.category),
                installedAt = installedAt,
                archived = archived,
            )
            LabeledActivity(app, appInfo.loadLabel(packageManager).toString())
        }
    }.let(::resolveLabelClashes)

    /** Android 15 lists archived apps too, their code offloaded until a launch restores it. */
    private fun isArchived(info: ApplicationInfo): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && info.isArchived
}
