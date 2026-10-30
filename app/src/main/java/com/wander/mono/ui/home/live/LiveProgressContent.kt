package com.wander.mono.ui.home.live

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.notifications.LiveActivity
import com.wander.mono.core.notifications.LiveProgress
import com.wander.mono.ui.components.WavyProgressLine
import com.wander.mono.ui.theme.LocalReducedMotion
import com.wander.mono.ui.theme.secondaryTextColor

/**
 * Something under way: badge | title over text | the percentage, then the wavy line, waving as it
 * advances. A progress of unknown length shows the indeterminate wave, or no line at all with
 * reduced motion, where it would only be motion.
 */
@Composable
internal fun LiveProgressContent(activity: LiveActivity) {
    val scheme = MaterialTheme.colorScheme
    val progress = activity.progress
    Column(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(GAP), verticalAlignment = Alignment.CenterVertically) {
            LiveBadge(R.drawable.sym_hourglass_bottom)
            LiveTitle(activity, Modifier.weight(1f))
            if (progress is LiveProgress.Determinate) {
                Text(
                    stringResource(R.string.live_progress_percent, percentOf(progress.fraction)),
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    color = scheme.primary,
                    maxLines = 1,
                )
            }
        }
        when (progress) {
            is LiveProgress.Determinate -> WavyProgressLine(
                fraction = { progress.fraction },
                isPlaying = true,
                color = scheme.primary,
                modifier = Modifier.padding(top = LINE_GAP),
            )
            LiveProgress.Indeterminate -> if (!LocalReducedMotion.current) {
                LinearWavyProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(top = LINE_GAP),
                    color = scheme.primary,
                    trackColor = scheme.primary.copy(alpha = TRACK_ALPHA),
                )
            }
            null -> Unit
        }
    }
}

/** Title over text, each on one line and each shown only when the notification has it. */
@Composable
internal fun LiveTitle(activity: LiveActivity, modifier: Modifier = Modifier) {
    Column(modifier) {
        activity.title?.let {
            Text(it, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        activity.text?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val GAP = 16.dp
private val LINE_GAP = 12.dp

/** The indeterminate line's track, as faint as the determinate line's unplayed side. */
private const val TRACK_ALPHA = 0.24f
