package com.wander.mono.core.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.ShortcutQuery
import android.content.pm.ShortcutInfo
import android.os.Build
import android.os.UserHandle
import com.wander.mono.di.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * Every shortcut of every app in every profile, for search and the long-press sheet. Android only
 * shares them with the default launcher, so the list is empty until Mono is the home app.
 */
@Singleton
class ShortcutRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val profiles: Profiles,
    events: CatalogEvents,
    @AppScope scope: CoroutineScope,
) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val hostAllowed = MutableStateFlow(launcherApps.hasShortcutHostPermission())

    val all: StateFlow<List<AppShortcut>> = combine(
        hostAllowed,
        events.changes.onStart { emit(CatalogChange.Profiles) },
    ) { allowed, _ -> allowed }
        .conflate()
        .map { allowed -> if (allowed) query() else emptyList() }
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** Becoming (or ceasing to be) the default home app sends no event; the home screen asks on start. */
    fun recheckHostPermission() {
        hostAllowed.value = launcherApps.hasShortcutHostPermission()
    }

    private fun query(): List<AppShortcut> = profiles.listed().flatMap { user -> queryProfile(user) }

    private fun queryProfile(user: UserHandle): List<AppShortcut> {
        val serial = profiles.serialOf(user)
        return readShortcuts(user).filter { it.isEnabled }.mapNotNull { info ->
            val label = info.shortLabel ?: info.longLabel ?: return@mapNotNull null
            AppShortcut(info.`package`, info.id, serial, label.toString())
        }
    }

    private fun readShortcuts(user: UserHandle): List<ShortcutInfo> = try {
        launcherApps.getShortcuts(ShortcutQuery().setQueryFlags(QUERY_FLAGS), user).orEmpty()
    } catch (_: IllegalStateException) {
        // A paused or locked profile has no readable shortcuts until it is unlocked; the
        // unlock broadcast reloads this.
        emptyList()
    } catch (_: SecurityException) {
        // Another app became the default home between the permission check and this read.
        hostAllowed.value = false
        emptyList()
    }

    private companion object {
        // Cached shortcuts (Android 11+) are conversations kept after the app removed them as dynamic.
        val QUERY_FLAGS = ShortcutQuery.FLAG_MATCH_DYNAMIC or ShortcutQuery.FLAG_MATCH_MANIFEST or
            ShortcutQuery.FLAG_MATCH_PINNED or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ShortcutQuery.FLAG_MATCH_CACHED else 0
    }
}
