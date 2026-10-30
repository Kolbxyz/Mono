package com.wander.mono.ui.drawer

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import kotlin.math.roundToInt

/**
 * The rail for TalkBack: one adjustable control named [label], reading the entry at [current] out
 * of [spoken] (★ said in words); swiping up or down moves to the next or previous one.
 */
internal fun Modifier.railSemantics(
    label: String,
    spoken: List<String>,
    current: Int,
    onSelect: (Int) -> Unit,
): Modifier = clearAndSetSemantics {
    contentDescription = label
    stateDescription = spoken.getOrNull(current).orEmpty()
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
