package com.wander.mono.core.system

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf

/**
 * What the home wallpaper looks like, as far as legibility goes: its main colours (ARGB) and
 * whether the system says dark text reads well on it.
 */
data class WallpaperTone(val colors: List<Int>, val supportsDarkText: Boolean)

/**
 * The home-screen wallpaper's [WallpaperTone], re-read when the system reports new wallpaper
 * colours (no polling). Null when Android cannot tell: before Android 12, which has no colour
 * hints, or for a live wallpaper that does not publish its colours.
 */
@Singleton
class Wallpaper @Inject constructor(@ApplicationContext private val context: Context) {
    val tone: Flow<WallpaperTone?> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        colorChanges().distinctUntilChanged()
    } else {
        flowOf(null)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun colorChanges(): Flow<WallpaperTone?> = callbackFlow {
        val manager = WallpaperManager.getInstance(context)
        val listener = WallpaperManager.OnColorsChangedListener { colors, which ->
            if (which and WallpaperManager.FLAG_SYSTEM != 0) trySend(colors?.toTone())
        }
        manager.addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
        trySend(manager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.toTone())
        awaitClose { manager.removeOnColorsChangedListener(listener) }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun WallpaperColors.toTone() = WallpaperTone(
    colors = listOfNotNull(primaryColor, secondaryColor, tertiaryColor).map { it.toArgb() },
    supportsDarkText = colorHints and WallpaperColors.HINT_SUPPORTS_DARK_TEXT != 0,
)
