package com.wander.mono.ui.sheet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R

/**
 * The usage chip's place in the header: kept at full height but invisible while [usage] is
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
    // While reading, a zero time stands in only to give the invisible chip its height.
    UsageChip(millis ?: 0L, hidden.padding(top = 6.dp).graphicsLayer { this.alpha = alpha })
}

/** "Today: 42 min" as a small tonal chip under the app's name in its long-press options. */
@Composable
private fun UsageChip(millis: Long, modifier: Modifier = Modifier) {
    val text = when (val time = usageTime(millis)) {
        UsageTime.UnderAMinute -> stringResource(R.string.sheet_usage_under_minute)
        is UsageTime.Minutes -> stringResource(R.string.sheet_usage_minutes, time.minutes)
        is UsageTime.Hours -> stringResource(R.string.sheet_usage_hours, time.hours, time.minutes)
    }
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        ) {
            Icon(painterResource(R.drawable.sym_schedule), contentDescription = null, modifier = Modifier.size(14.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}
