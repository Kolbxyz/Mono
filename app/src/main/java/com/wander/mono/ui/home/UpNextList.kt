package com.wander.mono.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.media.QueueEntry
import com.wander.mono.ui.components.rememberMonoHaptics
import com.wander.mono.ui.home.media.MediaArt
import com.wander.mono.ui.theme.textAlpha

/**
 * The next few items of the session's queue, each led by its own artwork when the session gives
 * one, else by its place in line; tapping one jumps playback to it.
 */
@Composable
internal fun UpNextList(entries: List<QueueEntry>, onPlay: (id: Long) -> Unit, modifier: Modifier = Modifier) {
    val haptics = rememberMonoHaptics()
    val playLabel = stringResource(R.string.media_play_this)
    val secondary = LocalContentColor.current.copy(alpha = textAlpha(SECONDARY_ALPHA))
    Column(modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.media_up_next),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 4.dp).semantics { heading() },
        )
        entries.forEachIndexed { index, entry ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClickLabel = playLabel) {
                        haptics.confirm()
                        onPlay(entry.id)
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                UpNextLead(entry, index)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(
                        entry.title,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    entry.subtitle?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

/** The entry's artwork, or its place in line (1 is next) in a slot as wide, so the titles align. */
@Composable
private fun UpNextLead(entry: QueueEntry, index: Int) {
    val art = entry.art
    if (art != null) {
        MediaArt(art, LEAD_SIZE)
    } else {
        Text(
            stringResource(R.string.media_up_next_place, index + 1),
            style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
            color = LocalContentColor.current.copy(alpha = textAlpha(SECONDARY_ALPHA)),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(LEAD_SIZE),
        )
    }
}

private val LEAD_SIZE = 40.dp

/** Secondary lines on the artwork-tinted card: dimmer, still in its contrasting content colour. */
private const val SECONDARY_ALPHA = 0.74f
