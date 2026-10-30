package com.wander.mono.ui.drawer

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import kotlin.math.roundToInt

/**
 * The rail for TalkBack: one adjustable control named [label], reading the entry at [current] out
 * of [spoken] (★ said in words); swiping up or down moves to the next or previous one. [actions]
 * are offered besides (locking or unlocking the private space).
 */
internal fun Modifier.railSemantics(
    label: String,
    spoken: List<String>,
    current: Int,
    onSelect: (Int) -> Unit,
    actions: List<CustomAccessibilityAction> = emptyList(),
): Modifier = clearAndSetSemantics {
    contentDescription = label
    stateDescription = spoken.getOrNull(current).orEmpty()
    if (actions.isNotEmpty()) customActions = actions
    if (spoken.size > 1) {
        progressBarRangeInfo = ProgressBarRangeInfo(
            current = current.toFloat(),
            range = 0f..spoken.lastIndex.toFloat(),
            steps = spoken.size - 2,
        )
        setProgress { target ->
            val index = target.roundToInt().coerceIn(0, spoken.lastIndex)
            if (index != current) onSelect(index)
            true
        }
    }
}
