package com.wander.mono.ui.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.data.settings.LIST_POSITIONS
import com.wander.mono.data.settings.LIST_POSITION_STEP
import com.wander.mono.ui.components.rememberMonoHaptics
import com.wander.mono.ui.home.listPositionLabel
import kotlin.math.roundToInt

/**
 * Where a short list sits on home: a press opens a slider over the toolbar, from the top of home
 * at its start to the bottom at its end. The list moves behind it while dragged, one haptic tick
 * per stop, so the user sees each place before letting go.
 */
@Composable
internal fun ListPositionButton(position: Int, onPosition: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val label = stringResource(R.string.edit_position, listPositionLabel(position))
    Box {
        IconButton(onClick = { open = true }) {
            Icon(painterResource(R.drawable.sym_height), contentDescription = label)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            Column(Modifier.width(MENU_WIDTH).padding(horizontal = MENU_INSET)) {
                Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
                PositionSlider(position, onPosition)
            }
        }
    }
}

@Composable
private fun PositionSlider(position: Int, onPosition: (Int) -> Unit) {
    val haptics = rememberMonoHaptics()
    val description = stringResource(R.string.settings_list_position)
    // The live drag, ahead of the saved value, so the thumb never lags the finger.
    var dragging by remember { mutableStateOf<Float?>(null) }
    Slider(
        value = dragging ?: position.toFloat(),
        onValueChange = { value ->
            val previous = dragging?.roundToInt() ?: position
            dragging = value
            val stop = value.roundToInt()
            if (stop != previous) haptics.segment()
            if (stop != position) onPosition(stop)
        },
        onValueChangeFinished = {
            dragging = null
            haptics.gestureEnd()
        },
        valueRange = LIST_POSITIONS.first.toFloat()..LIST_POSITIONS.last.toFloat(),
        steps = (LIST_POSITIONS.last - LIST_POSITIONS.first) / LIST_POSITION_STEP - 1,
        modifier = Modifier.semantics { contentDescription = description },
    )
}

private val MENU_WIDTH = 264.dp
private val MENU_INSET = 16.dp
