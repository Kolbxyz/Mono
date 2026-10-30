package com.wander.mono.ui.drawer

import android.os.Build
import android.view.Window
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
 * nothing at all, the wallpaper bare under shadowed text; the home scrim (the same legibility alpha
 * home computes from the wallpaper's colours); or, while composed, the wallpaper blurred behind the
 * window, the blur growing as the drawer is [revealed] (0 to 1): softly under a light tint, or
 * strongly under a lighter share of the home scrim. Blur needs Android 12 and the device's
 * cross-window blur, which battery saver or the device can turn off at any time: then the strong
 * blur's full scrim is drawn instead, and the blur comes back when Android allows it again. High
 * contrast always keeps a scrim, as on home.
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
            blurredBehind(SOFT_BLUR_RADIUS, revealed)
            homeScrimAlpha(SOFT_SCRIM, textShadow = false, highContrast = highContrast)
        }
        DrawerBackground.Scrim -> homeScrimAlpha(legible, textShadow = false, highContrast = highContrast)
        DrawerBackground.Blur -> {
            val base = if (blurredBehind(BLUR_RADIUS, revealed)) legible * BLUR_SCRIM else legible
            homeScrimAlpha(base, textShadow = false, highContrast = highContrast)
        }
    }
    return scheme.surface.copy(alpha = alpha)
}

/**
 * The shadow drawer text drawn on the wallpaper gets ([com.wander.mono.ui.home.onWallpaper]): none on
 * the opaque surface; home's text shadow where little or no tint is drawn; home's soft halo under a
 * scrim.
 */
@Composable
@ReadOnlyComposable
internal fun drawerTextShadow(background: DrawerBackground): Shadow? = when (background) {
    DrawerBackground.Solid -> null
    DrawerBackground.None, DrawerBackground.SoftBlur -> wallpaperTextShadow(textShadow = true, halo = 1f)
    DrawerBackground.Scrim, DrawerBackground.Blur -> wallpaperTextShadow(textShadow = false, halo = 1f)
}

/** Blurs the wallpaper behind the window by up to [radius]; true while Android allows it. */
@Composable
private fun blurredBehind(radius: Dp, revealed: () -> Float): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && windowBlur(radius, revealed)

/**
 * Blurs what is behind the activity's window (the wallpaper) while composed, up to [fullBlur] as far
 * as the drawer is [revealed] (0 to 1); true while Android allows cross-window blur, false when it
 * does not (battery saver, a device that cannot).
 */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun windowBlur(fullBlur: Dp, revealed: () -> Float): Boolean {
    val window: Window = LocalActivity.current?.window ?: return false
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

/** How strongly the wallpaper is blurred behind the fully open drawer. */
private val BLUR_RADIUS = 48.dp

/** The soft blur: the wallpaper still reads as itself, only its detail goes. */
private val SOFT_BLUR_RADIUS = 12.dp

/** How many steps the blur takes from none to its full radius as the drawer opens: a few px each. */
private const val BLUR_STEPS = 16

/** The blur evens the wallpaper out, so text needs this share of the home scrim on top of it. */
private const val BLUR_SCRIM = 0.6f

/** The light tint over the soft blur; the text shadow does the rest. */
private const val SOFT_SCRIM = 0.15f
