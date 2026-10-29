package com.wander.mono.core.apps

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Xml
import androidx.core.content.res.ResourcesCompat
import com.wander.mono.data.settings.IconSettings
import com.wander.mono.data.settings.SYSTEM_ICON_PACK
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.di.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import org.xmlpull.v1.XmlPullParserException

/** An installed icon pack. */
data class IconPack(val packageName: String, val label: String)

/**
 * Third-party icon packs, the ADW / Nova format most packs ship: found by the theme intents they
 * answer, with an `appfilter.xml` (an XML resource or an asset) mapping activities to drawables
 * in the pack. A pack's map is read once and kept until the pack's package changes ([invalidate]).
 */
@Singleton
class IconPacks @Inject constructor(
    @ApplicationContext context: Context,
    settings: SettingsStore,
    @AppScope scope: CoroutineScope,
) {
    private class LoadedPack(val resources: Resources, val filter: Map<PackComponent, String>)

    private val packageManager = context.packageManager
    private val loaded = ConcurrentHashMap<String, LoadedPack>()

    /** The chosen pack's package, [SYSTEM_ICON_PACK] for none; null until the setting is read. */
    val selected: StateFlow<String?> = settings[IconSettings.Pack].stateIn(scope, SharingStarted.Eagerly, null)

    suspend fun current(): String = selected.value ?: selected.filterNotNull().first()

    /** Installed packs, A to Z. Reads the package manager: call it off the main thread. */
    fun installed(): List<IconPack> = PACK_ACTIONS
        .flatMap { action -> activitiesFor(Intent(action)) }
        .map { it.activityInfo.applicationInfo }
        .distinctBy { it.packageName }
        .map { IconPack(it.packageName, it.loadLabel(packageManager).toString()) }
        .sortedBy { it.label.lowercase() }

    /** The pack's own launcher icon, for the picker; null once it is uninstalled. */
    fun packIcon(packageName: String): Drawable? = try {
        packageManager.getApplicationIcon(packageName)
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    /**
     * [pack]'s drawable for the activity, or null when the pack has none for it, so the app's own
     * icon shows. Blocking: called from the icon cache's IO dispatcher.
     */
    @SuppressLint("DiscouragedApi") // A pack's drawables are only known by the names its appfilter gives.
    fun icon(pack: String, packageName: String, className: String): Drawable? {
        val entry = load(pack) ?: return null
        val name = entry.filter[PackComponent(packageName, className)] ?: return null
        val id = entry.resources.getIdentifier(name, "drawable", pack)
        if (id == 0) return null
        return try {
            ResourcesCompat.getDrawable(entry.resources, id, null)
        } catch (_: Resources.NotFoundException) {
            null
        }
    }

    /** Forgets the maps of packs among [packageNames]: updated or removed, they are read again. */
    fun invalidate(packageNames: Set<String>) {
        packageNames.forEach(loaded::remove)
    }

    private fun load(pack: String): LoadedPack? {
        if (pack == SYSTEM_ICON_PACK) return null
        loaded[pack]?.let { return it }
        val resources = try {
            packageManager.getResourcesForApplication(pack)
        } catch (_: PackageManager.NameNotFoundException) {
            return null
        }
        return LoadedPack(resources, readFilter(pack, resources)).also { loaded[pack] = it }
    }

    /** The appfilter, from `res/xml` or else `assets`; empty when the pack ships a broken one. */
    @SuppressLint("DiscouragedApi")
    private fun readFilter(pack: String, resources: Resources): Map<PackComponent, String> = try {
        val xml = resources.getIdentifier(APPFILTER, "xml", pack)
        if (xml != 0) {
            val parser = resources.getXml(xml)
            try {
                appFilterMap(parser.appFilterItems())
            } finally {
                parser.close()
            }
        } else {
            resources.assets.open("$APPFILTER.xml").use { stream ->
                val parser = Xml.newPullParser()
                parser.setInput(stream, null)
                appFilterMap(parser.appFilterItems())
            }
        }
    } catch (_: XmlPullParserException) {
        emptyMap()
    } catch (_: IOException) {
        emptyMap()
    }

    private fun activitiesFor(intent: Intent): List<ResolveInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }

    private companion object {
        /** Declared in a pack's manifest: ADW's theme action, and Nova's. Report them in `<queries>`. */
        val PACK_ACTIONS = listOf("org.adw.launcher.THEMES", "com.novalauncher.THEME")
        const val APPFILTER = "appfilter"
    }
}
