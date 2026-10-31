package com.wander.mono.ui.home

import android.text.format.DateFormat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.theme.secondaryTextColor
import java.util.Date

/**
 * The focused favourite's line, under its name: a timer glyph, until when ("until 15:30", in the
 * user's 12 or 24-hour format) and "End focus".
 */
@Composable
fun FocusBar(row: FocusRow, onEnd: () -> Unit, modifier: Modifier = Modifier) {
    val until = DateFormat.getTimeFormat(LocalContext.current).format(Date(row.until))
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.sym_timer),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            stringResource(R.string.focus_until, until),
            style = MaterialTheme.typography.bodyMedium.onWallpaper(),
            color = secondaryTextColor(),
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onEnd) { Text(stringResource(R.string.focus_end)) }
    }
}

/** Rows a focus sets back fade to [FOCUS_DIM_ALPHA], still there to tap, on the effects spec. */
@Composable
internal fun Modifier.focusDim(dimmed: Boolean): Modifier {
    val alpha by animateFloatAsState(
        if (dimmed) FOCUS_DIM_ALPHA else 1f,
        MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "focus-dim",
    )
    return graphicsLayer { this.alpha = alpha }
}

private const val FOCUS_DIM_ALPHA = 0.38f
