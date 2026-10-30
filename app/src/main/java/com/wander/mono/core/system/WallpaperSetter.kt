package com.wander.mono.core.system

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The wallpapers Mono ships (`res/drawable-nodpi`, made by `tools/wallpapers/generate.py`): soft
 * gradients and blurred shapes, dark ones near true black for OLED screens, light pastels and one
 * monochrome.
 */
enum class BundledWallpaper(@DrawableRes val image: Int, @StringRes val label: Int) {
    Ink(R.drawable.wallpaper_ink, R.string.wallpaper_ink),
    Ember(R.drawable.wallpaper_ember, R.string.wallpaper_ember),
    Moss(R.drawable.wallpaper_moss, R.string.wallpaper_moss),
    Graphite(R.drawable.wallpaper_graphite, R.string.wallpaper_graphite),
    Dusk(R.drawable.wallpaper_dusk, R.string.wallpaper_dusk),
    Dune(R.drawable.wallpaper_dune, R.string.wallpaper_dune),
    Mist(R.drawable.wallpaper_mist, R.string.wallpaper_mist),
    Sage(R.drawable.wallpaper_sage, R.string.wallpaper_sage),
}

/** Which screens a wallpaper goes on, as [WallpaperManager]'s `which` flags. */
enum class WallpaperTarget(val flags: Int) {
    Home(WallpaperManager.FLAG_SYSTEM),
    Lock(WallpaperManager.FLAG_LOCK),
    Both(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK),
}

/**
 * Sets a [BundledWallpaper] through [WallpaperManager] (`SET_WALLPAPER`, a normal permission). The
 * system decodes, crops and stores the image, then reports new wallpaper colours, which the
 * wallpaper palette and [Wallpaper] follow on their own.
 */
@Singleton
class WallpaperSetter @Inject constructor(@ApplicationContext private val context: Context) {
    /** Writes a few hundred kilobytes and waits on the wallpaper service, so off the main thread. */
    suspend fun apply(wallpaper: BundledWallpaper, target: WallpaperTarget): ActionResult =
        withContext(Dispatchers.IO) { applyNow(wallpaper, target) }

    private fun applyNow(wallpaper: BundledWallpaper, target: WallpaperTarget): ActionResult {
        val manager = WallpaperManager.getInstance(context)
        // A device policy (work profile, kiosk) may forbid changing the wallpaper.
        val allowed = manager.isWallpaperSupported && manager.isSetWallpaperAllowed
        return if (allowed) write(manager, wallpaper, target) else ActionResult.Failed(R.string.wallpaper_not_allowed)
    }

    // setResource reads the file's raw bytes; a drawable-nodpi WebP is packaged as it is, so it is
    // the image itself, not the raw resource lint expects.
    @SuppressLint("ResourceType")
    private fun write(manager: WallpaperManager, wallpaper: BundledWallpaper, target: WallpaperTarget): ActionResult =
        try {
            manager.setResource(wallpaper.image, target.flags)
            ActionResult.Done
        } catch (_: IOException) {
            ActionResult.Failed(R.string.wallpaper_failed)
        } catch (_: SecurityException) {
            ActionResult.Failed(R.string.wallpaper_not_allowed)
        }
}
