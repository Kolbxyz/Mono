package com.wander.mono.ui.sheet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.wander.mono.R

/**
 * The usage line's place in the header: kept at full height but invisible while [usage] is
 * [SheetUsage.Reading], so the header does not grow under the opening sheet, then faded in.
 */
@Composable
internal fun UsageSlot(usage: SheetUsage) {
    val millis = (usage as? SheetUsage.Today)?.millis
    val alpha by animateFloatAsState(
        targetValue = if (millis != null) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "usage",
    )
    val hidden = if (millis == null) Modifier.clearAndSetSemantics {} else Modifier
    // While reading, a zero time stands in only to give the invisible line its height.
    UsageLine(millis ?: 0L, hidden.graphicsLayer { this.alpha = alpha })
}

/** "Today: 42 min" under the app's name in its long-press options. */
@Composable
internal fun UsageLine(millis: Long, modifier: Modifier = Modifier) {
    val text = when (val time = usageTime(millis)) {
        UsageTime.UnderAMinute -> stringResource(R.string.sheet_usage_under_minute)
        is UsageTime.Minutes -> stringResource(R.string.sheet_usage_minutes, time.minutes)
        is UsageTime.Hours -> stringResource(R.string.sheet_usage_hours, time.hours, time.minutes)
    }
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
