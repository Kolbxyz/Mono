package com.wander.mono.ui.drawer

import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.data.settings.DrawerBackground
import com.wander.mono.ui.home.homeScrimAlpha
import com.wander.mono.ui.home.scrimAlpha
import com.wander.mono.ui.home.wallpaperTextShadow
import java.util.function.Consumer

/**
 * The colour the drawer is drawn on, per the user's [DrawerLook.background]: the opaque surface;
 * nothing at all, the wallpaper bare under shadowed text; or an even tonal veil of the surface
 * colour at a measured alpha (the legibility alpha home computes from the wallpaper's colours), never
 * a gradient or a vignette. The soft blur also blurs the wallpaper behind the window while composed,
 * the blur growing as the drawer is [revealed] (0 to 1); the blur evens the wallpaper out, so its veil
 * can be lighter and the wallpaper still reads as itself. Blur needs Android 12, the device's
 * cross-window blur (battery saver or the device can turn it off at any time: the full veil is drawn
 * meanwhile) and the wallpaper behind the window: under "solid black wallpaper" there is nothing to
 * blur and no blur pass runs. High contrast always raises the veil, as on home.
 */
@Composable
internal fun drawerBackdrop(look: DrawerLook, revealed: () -> Float): Color {
    val scheme = MaterialTheme.colorScheme
    val legible = remember(look.tone, scheme.surface, scheme.onSurface) {
        scrimAlpha(look.tone, scheme.surface.toArgb(), scheme.onSurface.toArgb())
    }
    val highContrast = look.highContrast
    val alpha = when (look.background) {
        DrawerBackground.Solid -> return scheme.surface
        // As home with a text shadow: no scrim, the shadow carries the text.
        DrawerBackground.None -> homeScrimAlpha(legible, textShadow = true, highContrast = highContrast)
        DrawerBackground.SoftBlur -> {
            val base = if (blurredBehind(SOFT_BLUR_RADIUS, revealed)) legible * SOFT_BLUR_VEIL else legible
            homeScrimAlpha(base, textShadow = false, highContrast = highContrast)
        }
        DrawerBackground.Scrim -> homeScrimAlpha(legible, textShadow = false, highContrast = highContrast)
    }
    return scheme.surface.copy(alpha = alpha)
}

/**
 * The shadow drawer text gets ([com.wander.mono.ui.home.onWallpaper]): only on the bare wallpaper,
 * where nothing else carries it. Over a veil the text is legible on its own, and a shadow or halo
 * under every row and letter would only smudge the even tone.
 */
@Composable
@ReadOnlyComposable
internal fun drawerTextShadow(background: DrawerBackground): Shadow? = when (background) {
    DrawerBackground.None -> wallpaperTextShadow(textShadow = true, halo = 1f)
    DrawerBackground.SoftBlur, DrawerBackground.Scrim, DrawerBackground.Solid -> null
}

/** Blurs the wallpaper behind the window by up to [radius]; true while Android allows it. */
@Composable
private fun blurredBehind(radius: Dp, revealed: () -> Float): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && windowBlur(radius, revealed)

/**
 * Blurs what is behind the activity's window (the wallpaper) while composed, up to [fullBlur] as far
 * as the drawer is [revealed] (0 to 1); true while Android allows cross-window blur, false when it
 * does not (battery saver, a device that cannot) or when the window shows no wallpaper to blur.
 */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun windowBlur(fullBlur: Dp, revealed: () -> Float): Boolean {
    val window: Window = LocalActivity.current?.window ?: return false
    // An opaque window ("solid black wallpaper") has nothing behind it worth a blur pass.
    if (window.attributes.flags and WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER == 0) return false
    val fullRadius = with(LocalDensity.current) { fullBlur.roundToPx() }
    var enabled by remember { mutableStateOf(window.windowManager.isCrossWindowBlurEnabled) }
    // Changes only at each step, fine enough to read as a smooth ramp without a window update on
    // every frame of the drawer's motion.
    val radius by remember(revealed, fullRadius) {
        derivedStateOf { steppedBlur(fullRadius, revealed(), BLUR_STEPS) }
    }
    DisposableEffect(window) {
        val listener = Consumer<Boolean> { enabled = it }
        window.windowManager.addCrossWindowBlurEnabledListener(listener)
        onDispose {
            window.windowManager.removeCrossWindowBlurEnabledListener(listener)
            window.setBackgroundBlurRadius(0)
        }
    }
    LaunchedEffect(window, enabled, radius) { window.setBackgroundBlurRadius(if (enabled) radius else 0) }
    return enabled
}

/** The soft blur: the wallpaper still reads as itself, only its detail goes. */
private val SOFT_BLUR_RADIUS = 20.dp

/** How many steps the blur takes from none to its full radius as the drawer opens: a few px each. */
private const val BLUR_STEPS = 16

/**
 * The blur evens the wallpaper out, so the veil over it needs only this share of home's measured
 * scrim to keep text legible without any shadow.
 */
private const val SOFT_BLUR_VEIL = 0.7f
