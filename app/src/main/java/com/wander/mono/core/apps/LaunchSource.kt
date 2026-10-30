package com.wander.mono.core.apps

import android.graphics.Rect
import android.os.Bundle

/**
 * Where on screen a launch starts (spec 7.4.4): the tapped row's [bounds] in screen coordinates,
 * which the app learns as its source bounds, and the [options] of the window growing out of them.
 */
class LaunchSource(val bounds: Rect, val options: Bundle)
