package com.wander.mono.ui.drawer

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp

/**
 * The fast-scroll rail on the right edge: touch or slide along it to jump to a letter. A touch
 * shortcut only; screen readers scroll the list itself, so the rail is hidden from them.
 */
@Composable
fun LetterRail(sections: List<Section>, onSelect: (Section) -> Unit, modifier: Modifier = Modifier) {
    var active by remember { mutableStateOf<String?>(null) }
    val haptics = LocalHapticFeedback.current
    val currentSections by rememberUpdatedState(sections)
    val currentOnSelect by rememberUpdatedState(onSelect)

    Column(
        modifier
            .fillMaxHeight()
            .width(32.dp)
            .clearAndSetSemantics {}
            .pointerInput(Unit) {
                fun select(y: Float) {
                    val list = currentSections
                    if (list.isEmpty()) return
                    val section = list[(y / size.height * list.size).toInt().coerceIn(0, list.lastIndex)]
                    if (section.letter != active) {
                        active = section.letter
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        currentOnSelect(section)
                    }
                }
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    select(down.position.y)
                    do {
                        val event = awaitPointerEvent()
                        event.changes.filter { it.pressed }.forEach { change ->
                            change.consume()
                            select(change.position.y)
                        }
                    } while (event.changes.any { it.pressed })
                    active = null
                }
            },
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        sections.forEach { section ->
            Text(
                section.letter,
                style = MaterialTheme.typography.labelMedium,
                color = if (section.letter == active) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}
