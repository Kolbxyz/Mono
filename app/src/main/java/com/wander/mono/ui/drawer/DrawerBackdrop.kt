package com.wander.mono.ui.drawer

import android.os.Build
import android.view.Window
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wander.mono.data.settings.DrawerBackground
import com.wander.mono.ui.home.homeScrimAlpha
import com.wander.mono.ui.home.scrimAlpha
import java.util.function.Consumer

/**
 * The colour the drawer is drawn on, per the user's [DrawerLook.background]: the opaque surface;
 * the home scrim (the same legibility alpha home computes from the wallpaper's colours); or, while
 * composed, the wallpaper blurred behind the window under a lighter scrim, the blur growing as the
 * drawer is [revealed] (0 to 1). Blur needs Android 12 and the device's cross-window blur, which
 * battery saver or the device can turn off at any time: then the plain scrim is drawn instead, and
 * the blur comes back when Android allows it again.
 */
@Composable
internal fun drawerBackdrop(look: DrawerLook, revealed: () -> Float): Color {
    val scheme = MaterialTheme.colorScheme
    if (look.background == DrawerBackground.Solid) return scheme.surface
    val blurred = look.background == DrawerBackground.Blur &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        windowBlur(revealed)
    val legible = remember(look.tone, scheme.surface, scheme.onSurface) {
        scrimAlpha(look.tone, scheme.surface.toArgb(), scheme.onSurface.toArgb())
    }
    val base = if (blurred) legible * BLUR_SCRIM else legible
    return scheme.surface.copy(alpha = homeScrimAlpha(base, textShadow = false, highContrast = look.highContrast))
}

/**
 * Blurs what is behind the activity's window (the wallpaper) while composed, as far as the drawer
 * is [revealed] (0 to 1); true while Android allows cross-window blur, false when it does not
 * (battery saver, a device that cannot).
 */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun windowBlur(revealed: () -> Float): Boolean {
    val window: Window = LocalActivity.current?.window ?: return false
    val fullRadius = with(LocalDensity.current) { BLUR_RADIUS.roundToPx() }
    var enabled by remember { mutableStateOf(window.windowManager.isCrossWindowBlurEnabled) }
    // Changes only at each of the few steps, not every frame of the drawer's motion.
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

/** How many steps the blur takes from none to [BLUR_RADIUS] as the drawer opens. */
private const val BLUR_STEPS = 4

/** The blur evens the wallpaper out, so text needs this share of the home scrim on top of it. */
private const val BLUR_SCRIM = 0.6f
