package com.wander.mono.ui.drawer

import kotlin.math.roundToInt

/**
 * The window blur for a drawer [shown] that far (0 to 1) of a full [radius], in [steps] even steps:
 * the blur grows and fades with the drawer without a window update every frame. [steps] is positive.
 */
internal fun steppedBlur(radius: Int, shown: Float, steps: Int): Int {
    val step = (shown.coerceIn(0f, 1f) * steps).roundToInt()
    return radius * step / steps
}
