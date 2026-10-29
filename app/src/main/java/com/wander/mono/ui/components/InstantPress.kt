package com.wander.mono.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Reports a press to [source] the instant a finger lands, instead of after a click's short delay
 * inside a scrolling list. Nothing is consumed, so clicks, long presses and scrolling are
 * untouched, and a touch that turns into a scroll cancels the press.
 */
fun Modifier.instantPress(source: MutableInteractionSource): Modifier = pointerInput(source) {
    val slop = viewConfiguration.touchSlop
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val press = PressInteraction.Press(down.position)
        source.tryEmit(press)
        var cancelled = false
        while (true) {
            val change = awaitPointerEvent(PointerEventPass.Initial).changes
                .firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break
            if ((change.position - down.position).getDistance() > slop) {
                cancelled = true
                break
            }
        }
        source.tryEmit(if (cancelled) PressInteraction.Cancel(press) else PressInteraction.Release(press))
    }
}
