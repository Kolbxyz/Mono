package com.wander.mono.core.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.wander.mono.data.settings.IconSettings
import com.wander.mono.data.settings.IconShape
import com.wander.mono.data.settings.SYSTEM_ICON_PACK
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.di.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

/**
 * In-memory icon cache, keyed by app, by whether the themed (monochrome) icon was asked for, by
 * the icon pack in use and by size (a large icon is never a small one scaled up), bounded by bytes
 * since sizes differ. Full-colour icons come from the chosen pack when it has one for the app,
 * else the app's own; they are profile-badged. Themed ones are not, the row's label carries the
 * work badge. Entries are evicted per package when `LauncherApps` reports a change, and all of
 * them when the chosen pack itself changes. With an icon shape chosen, adaptive icons of Mono's
 * own profile come unmasked ([fullBleedBitmap]) for the UI to cut; work icons keep the system's
 * mask, which their badge sits on. Each icon carries its app's [AppIconImage.accent], read from
 * the full-colour icon while it is decoded anyway (from the app's own icon for a themed one, which
 * packs do not apply to), so rows and themed glyphs can take the app's colour at no extra load.
 */
@Singleton
class IconCache @Inject constructor(
    @ApplicationContext context: Context,
    private val profiles: Profiles,
    private val packs: IconPacks,
    settings: SettingsStore,
    @AppScope scope: CoroutineScope,
) {
    private data class Entry(
        val key: AppKey,
        val themed: Boolean,
        val pack: String,
        val unmasked: Boolean,
        val sizePx: Int,
    )

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val packageManager = context.packageManager
    private val cache = object : LruCache<Entry, AppIconImage>(MAX_BYTES) {
        override fun sizeOf(key: Entry, value: AppIconImage) = value.bitmap.asAndroidBitmap().allocationByteCount
    }

    /** Whether a shape other than the system's is chosen; null until the setting is read. */
    private val unmasked: StateFlow<Boolean?> = settings[IconSettings.Shape]
        .map { it != IconShape.System }
        .stateIn(scope, SharingStarted.Eagerly, null)

    /**
     * Null when the activity no longer exists, e.g. the app was just uninstalled. A [themed] request
     * gives the full-colour icon when the app has no monochrome layer.
     */
    suspend fun icon(key: AppKey, themed: Boolean, sizePx: Int): AppIconImage? {
        // Packs apply to full-colour icons only; themed icons are the apps' own glyphs.
        val unmask = unmasked.value ?: unmasked.filterNotNull().first()
        val entry = Entry(key, themed, if (themed) SYSTEM_ICON_PACK else packs.current(), unmask, sizePx)
        cache.get(entry)?.let { return it }
        return withContext(Dispatchers.IO) { load(entry, sizePx) }?.also { cache.put(entry, it) }
    }

    /**
     * The icon [icon] would give, if it is in memory already; null otherwise, or while the settings
     * it depends on are still being read. Never loads: cheap enough for the first frame of a row.
     */
    fun cached(key: AppKey, themed: Boolean, sizePx: Int): AppIconImage? {
        val unmask = unmasked.value
        val pack = if (themed) SYSTEM_ICON_PACK else packs.selected.value
        return if (unmask == null || pack == null) null else cache.get(Entry(key, themed, pack, unmask, sizePx))
    }

    fun evict(packageNames: Set<String>) {
        packs.invalidate(packageNames)
        cache.snapshot().keys
            .filter { it.key.packageName in packageNames || it.pack in packageNames }
            .forEach { cache.remove(it) }
    }

    fun evictAll() = cache.evictAll()

    private fun load(entry: Entry, sizePx: Int): AppIconImage? = resolve(entry.key)?.let { info ->
        val themed = if (entry.themed) themedIcon(info.getIcon(0), sizePx) else null
        themed ?: colourIcon(entry, info, sizePx)
    }

    /** The app's monochrome glyph, with the accent of the full-colour icon it stands in for. */
    private fun themedIcon(own: Drawable, sizePx: Int): AppIconImage? = monochromeBitmap(own, sizePx)?.let {
        AppIconImage(it.asImageBitmap(), isMonochrome = true, accent = own.accentColour())
    }

    /**
     * The pack's icon for the activity when it has one, else the app's own; badged for its profile,
     * or unmasked when a shape is chosen and the icon is adaptive.
     */
    private fun colourIcon(entry: Entry, info: LauncherActivityInfo, sizePx: Int): AppIconImage {
        val packIcon = packs.icon(entry.pack, entry.key.packageName, entry.key.className)
        if (entry.unmasked && info.user == profiles.main) {
            fullBleedBitmap(packIcon ?: info.getIcon(0), sizePx)?.let { bitmap ->
                val accent = bitmap.accentColour()
                return AppIconImage(bitmap.asImageBitmap(), isMonochrome = false, isFullBleed = true, accent = accent)
            }
        }
        val icon = packIcon?.let { packageManager.getUserBadgedIcon(it, info.user) } ?: info.getBadgedIcon(0)
        val bitmap = icon.toBitmap(sizePx, sizePx)
        return AppIconImage(bitmap.asImageBitmap(), isMonochrome = false, accent = bitmap.accentColour())
    }

    private fun resolve(key: AppKey): LauncherActivityInfo? {
        val user = profiles.userOf(key.userSerial) ?: return null
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(key.packageName, key.className))
        return launcherApps.resolveActivity(intent, user)
    }

    private companion object {
        // A full drawer and home at a couple of sizes: a 48dp icon at xxxhdpi is ~150 KB.
        const val MAX_BYTES = 24 * 1024 * 1024
    }
}
