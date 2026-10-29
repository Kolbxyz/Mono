package com.wander.mono.core.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** In-memory icon cache. Profile-badged icons, evicted per package when `LauncherApps` reports a change. */
@Singleton
class IconCache @Inject constructor(
    @ApplicationContext context: Context,
    private val profiles: Profiles,
) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val cache = LruCache<AppKey, ImageBitmap>(MAX_ICONS)

    /** Null when the activity no longer exists, e.g. the app was just uninstalled. */
    suspend fun icon(key: AppKey, sizePx: Int): ImageBitmap? {
        cache.get(key)?.let { return it }
        return withContext(Dispatchers.IO) { load(key, sizePx) }?.also { cache.put(key, it) }
    }

    fun evict(packageNames: Set<String>) {
        cache.snapshot().keys.filter { it.packageName in packageNames }.forEach { cache.remove(it) }
    }

    fun evictAll() = cache.evictAll()

    private fun load(key: AppKey, sizePx: Int): ImageBitmap? {
        val user = profiles.userOf(key.userSerial) ?: return null
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(key.packageName, key.className))
        return launcherApps.resolveActivity(intent, user)?.getBadgedIcon(0)?.toBitmap(sizePx, sizePx)?.asImageBitmap()
    }

    private companion object {
        // Roughly a full drawer's worth; a 48dp icon at xxxhdpi is ~150 KB.
        const val MAX_ICONS = 300
    }
}
