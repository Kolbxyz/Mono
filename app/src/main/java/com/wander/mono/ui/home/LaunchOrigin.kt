package com.wander.mono.ui.home

import android.app.ActivityOptions
import android.graphics.Rect
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import com.wander.mono.core.apps.LaunchSource

/**
 * The place a tapped row launches from. It only keeps the row's layout, which it reads at the tap,
 * so scrolling costs nothing; [source] turns it into screen bounds and a clip-reveal from them.
 */
class LaunchOrigin internal constructor(private val view: View) {
    private var coordinates: LayoutCoordinates? = null

    /** Where the row is drawn; put on the row whose bounds the app should grow out of. */
    val modifier: Modifier = Modifier.onGloballyPositioned { coordinates = it }

    /** The row's bounds now, or null when it is no longer laid out (the system animates then). */
    fun source(): LaunchSource? {
        val layout = coordinates?.takeIf { it.isAttached } ?: return null
        // Relative to the compose root, which is the view the reveal is measured from.
        val box = layout.boundsInRoot()
        val left = box.left.toInt()
        val top = box.top.toInt()
        val width = box.width.toInt()
        val height = box.height.toInt()
        val onScreen = IntArray(2).also(view::getLocationOnScreen)
        val bounds = Rect(onScreen[0] + left, onScreen[1] + top, onScreen[0] + left + width, onScreen[1] + top + height)
        val options = ActivityOptions.makeClipRevealAnimation(view, left, top, width, height).toBundle()
        return LaunchSource(bounds, options)
    }
}

@Composable
fun rememberLaunchOrigin(): LaunchOrigin {
    val view = LocalView.current
    return remember(view) { LaunchOrigin(view) }
}
