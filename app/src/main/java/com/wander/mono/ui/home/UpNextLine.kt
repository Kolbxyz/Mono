package com.wander.mono.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.media.QueueEntry
import com.wander.mono.ui.components.rememberMonoHaptics
import com.wander.mono.ui.home.media.MediaArt
import com.wander.mono.ui.theme.textAlpha

/**
 * What plays next, on one line so the player stays short: a pill led by the item's artwork when
 * the session gives one, "Up next", then its title · subtitle; tapping it jumps playback there.
 */
@Composable
internal fun UpNextLine(entry: QueueEntry, onPlay: (id: Long) -> Unit, modifier: Modifier = Modifier) {
    val haptics = rememberMonoHaptics()
    val content = LocalContentColor.current
    val secondary = content.copy(alpha = textAlpha(SECONDARY_ALPHA))
    val separator = stringResource(R.string.media_title_separator)
    val line = buildAnnotatedString {
        append(entry.title)
        entry.subtitle?.let { subtitle ->
            withStyle(SpanStyle(color = secondary)) {
                append(separator)
                append(subtitle)
            }
        }
    }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = LINE_HEIGHT)
            .clip(CircleShape)
            .background(content.copy(alpha = LINE_VEIL))
            .clickable(onClickLabel = stringResource(R.string.media_play_this)) {
                haptics.confirm()
                onPlay(entry.id)
            }
            .padding(start = if (entry.art != null) ART_INSET else TEXT_INSET, end = TEXT_INSET),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        entry.art?.let { MediaArt(it, LEAD_SIZE) }
        Text(stringResource(R.string.media_up_next), style = MaterialTheme.typography.labelLarge, color = secondary)
        Text(
            line,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

/** A touch target tall, no more. */
private val LINE_HEIGHT = 48.dp
private val LEAD_SIZE = 32.dp

/** Round artwork sits close to the pill's curve; text keeps the room the curve needs. */
private val ART_INSET = 8.dp
private val TEXT_INSET = 16.dp

/** The pill's veil, lighter than the transport's so the line stays secondary. */
private const val LINE_VEIL = 0.10f

/** Secondary words on the artwork-tinted card: dimmer, still in its contrasting content colour. */
private const val SECONDARY_ALPHA = 0.74f
