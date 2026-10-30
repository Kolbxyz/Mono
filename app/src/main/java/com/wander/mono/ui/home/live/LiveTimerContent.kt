package com.wander.mono.ui.home.live

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.notifications.LiveActivity
import com.wander.mono.core.notifications.PreviewAction
import com.wander.mono.ui.theme.secondaryTextColor

/**
 * A timer or stopwatch: its name small above large tabular digits that count while the row is on
 * screen, then the notification's own first two buttons ("Pause", "Stop") as equal tonal buttons.
 * A paused timer has no running chronometer: its text (the time left, as the app words it) takes
 * the digits' place.
 */
@Composable
internal fun LiveTimerContent(activity: LiveActivity, tone: LiveTone, onAction: (PreviewAction) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val chronometer = activity.chronometer
    Column(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(GAP), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                // Running, the title names it over the digits; paused, the text is the large line.
                val label = if (chronometer != null) activity.title else activity.title.takeIf { activity.text != null }
                val headline = if (chronometer != null) null else activity.text ?: activity.title
                label?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelLarge,
                        color = secondaryTextColor(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (chronometer != null) {
                    ChronometerText(chronometer, MaterialTheme.typography.displayMedium, scheme.onSurface)
                } else {
                    headline?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            LiveBadge(R.drawable.sym_timer, tone)
        }
        val actions = activity.actions.take(MAX_ACTIONS)
        if (actions.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(BUTTON_GAP),
                modifier = Modifier.fillMaxWidth().padding(top = GAP),
            ) {
                actions.forEach { action ->
                    FilledTonalButton(
                        onClick = { onAction(action) },
                        modifier = Modifier.weight(1f).heightIn(min = BUTTON_HEIGHT),
                    ) {
                        Text(action.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

/** Two buttons at most, so the row stays a glance and each button keeps a readable width. */
private const val MAX_ACTIONS = 2

private val GAP = 16.dp
private val BUTTON_GAP = 8.dp
private val BUTTON_HEIGHT = 48.dp
