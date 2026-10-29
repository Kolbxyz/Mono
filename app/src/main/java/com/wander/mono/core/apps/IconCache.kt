package com.wander.mono.core.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.util.LruCache
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * In-memory icon cache, keyed by app and by whether the themed (monochrome) icon was asked for.
 * Full-colour icons are profile-badged; themed ones are not, the row's label carries the work badge.
 * Entries are evicted per package when `LauncherApps` reports a change.
 */
@Singleton
class IconCache @Inject constructor(
    @ApplicationContext context: Context,
    private val profiles: Profiles,
) {
    private data class Entry(val key: AppKey, val themed: Boolean)

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val cache = LruCache<Entry, AppIconImage>(MAX_ICONS)

    /**
     * Null when the activity no longer exists, e.g. the app was just uninstalled. A [themed] request
     * gives the full-colour icon when the app has no monochrome layer.
     */
    suspend fun icon(key: AppKey, themed: Boolean, sizePx: Int): AppIconImage? {
        val entry = Entry(key, themed)
        cache.get(entry)?.let { return it }
        return withContext(Dispatchers.IO) { load(entry, sizePx) }?.also { cache.put(entry, it) }
    }

    fun evict(packageNames: Set<String>) {
        cache.snapshot().keys.filter { it.key.packageName in packageNames }.forEach { cache.remove(it) }
    }

    fun evictAll() = cache.evictAll()

    private fun load(entry: Entry, sizePx: Int): AppIconImage? {
        val info = resolve(entry.key) ?: return null
        val monochrome = if (entry.themed) monochromeBitmap(info.getIcon(0), sizePx) else null
        return if (monochrome != null) {
            AppIconImage(monochrome.asImageBitmap(), isMonochrome = true)
        } else {
            AppIconImage(info.getBadgedIcon(0).toBitmap(sizePx, sizePx).asImageBitmap(), isMonochrome = false)
        }
    }

    private fun resolve(key: AppKey): LauncherActivityInfo? {
        val user = profiles.userOf(key.userSerial) ?: return null
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(key.packageName, key.className))
        return launcherApps.resolveActivity(intent, user)
    }

    private companion object {
        // Roughly a full drawer's worth; a 48dp icon at xxxhdpi is ~150 KB.
        const val MAX_ICONS = 300
    }
}
