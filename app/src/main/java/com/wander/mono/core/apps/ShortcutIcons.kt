package com.wander.mono.core.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.ShortcutQuery
import android.content.pm.ShortcutInfo
import android.util.LruCache
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.wander.mono.di.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The icons shortcuts publish (a contact's face, a chat's avatar), for search results. A small
 * in-memory LRU: search shows a handful at a time. Entries go when `LauncherApps` reports the
 * package's shortcuts, the package or a profile changed.
 */
@Singleton
class ShortcutIcons @Inject constructor(
    @ApplicationContext context: Context,
    private val profiles: Profiles,
    events: CatalogEvents,
    @AppScope scope: CoroutineScope,
) {
    private data class Entry(val packageName: String, val id: String, val userSerial: Long)

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val densityDpi = context.resources.displayMetrics.densityDpi
    private val cache = LruCache<Entry, AppIconImage>(MAX_ICONS)

    init {
        scope.launch {
            events.changes.collect { change ->
                when (change) {
                    is CatalogChange.Shortcuts -> evict(setOf(change.packageName))
                    is CatalogChange.Packages -> evict(change.packageNames)
                    CatalogChange.Profiles -> cache.evictAll()
                }
            }
        }
    }

    /** Null when the shortcut has no icon of its own, is gone, or its profile is locked. */
    suspend fun icon(shortcut: AppShortcut, sizePx: Int): AppIconImage? {
        val entry = Entry(shortcut.packageName, shortcut.id, shortcut.userSerial)
        cache.get(entry)?.let { return it }
        return withContext(Dispatchers.IO) { load(entry, sizePx) }?.also { cache.put(entry, it) }
    }

    private fun load(entry: Entry, sizePx: Int): AppIconImage? {
        val info = find(entry) ?: return null
        val drawable = launcherApps.getShortcutIconDrawable(info, densityDpi) ?: return null
        return AppIconImage(drawable.toBitmap(sizePx, sizePx).asImageBitmap(), isMonochrome = false)
    }

    private fun find(entry: Entry): ShortcutInfo? {
        val user = profiles.userOf(entry.userSerial) ?: return null
        val query = ShortcutQuery()
            .setPackage(entry.packageName)
            .setShortcutIds(listOf(entry.id))
            .setQueryFlags(SHORTCUT_QUERY_FLAGS)
        return try {
            launcherApps.getShortcuts(query, user)?.firstOrNull()
        } catch (_: IllegalStateException) {
            // The profile is paused or locked.
            null
        } catch (_: SecurityException) {
            // Mono stopped being the default home app.
            null
        }
    }

    private fun evict(packageNames: Set<String>) {
        cache.snapshot().keys.filter { it.packageName in packageNames }.forEach { cache.remove(it) }
    }

    private companion object {
        const val MAX_ICONS = 48
    }
}
