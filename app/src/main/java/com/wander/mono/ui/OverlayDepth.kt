package com.wander.mono.ui

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp

/**
 * One depth system for every overlay, so opening one over another never stacks two scrims:
 * - the drawer: home under it fades and shrinks back ([fadeBehindDrawer], [shrinkBehindDrawer]);
 *   the drawer's own backdrop is the only scrim.
 * - settings: everything under it (home, and the drawer when opened from there) recedes as one,
 *   under one scrim drawn here ([recede] with [overlayScrim]).
 * - a sheet: everything under it, settings included, recedes a little less; the sheet's own scrim,
 *   the same colour as [overlayScrim], is the only one.
 * Each recedes by its overlay's cover, which a predictive back swipe hands back, so the swipe seeks
 * the whole stack at once.
 */
fun Modifier.recede(cover: () -> Float, depth: OverlayLevel, scrim: Color? = null): Modifier = this
    .then(
        if (scrim == null) {
            Modifier
        } else {
            // Outside the scale, so the scrim covers the window edge to edge while the layers shrink.
            Modifier.drawWithContent {
                drawContent()
                val amount = cover()
                if (amount > 0f) drawRect(scrim, alpha = amount)
            }
        },
    )
    .graphicsLayer {
        val scale = lerp(1f, depth.scale, cover())
        scaleX = scale
        scaleY = scale
    }

/** How far the layers under each kind of overlay shrink back when it is fully in. */
enum class OverlayLevel(val scale: Float) {
    /** The drawer and settings take the whole window. */
    Screen(SCREEN_SCALE),

    /** A sheet takes the lower part; the rest only needs to step back. */
    Sheet(SHEET_SCALE),
}

private const val SCREEN_SCALE = 0.92f
private const val SHEET_SCALE = 0.96f

/** The one scrim between an overlay and the layers under it: Material's own sheet scrim colour. */
@Composable
fun overlayScrim(): Color = BottomSheetDefaults.ScrimColor
