package com.wander.mono.ui.sheet

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R

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
