package com.wander.mono.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.media.PlayingMedia
import kotlin.math.abs

/** The playing media, inline under its app: artwork, title, artist and transport, tinted by the artwork. */
@Composable
fun NowPlayingCard(
    media: PlayingMedia,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val tint = media.artColor?.let { lerp(scheme.surfaceContainerHigh, Color(it), ART_TINT) }
        ?: scheme.surfaceContainerHigh
    val container by animateColorAsState(
        targetValue = tint,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "art-tint",
    )
    val content = mostContrasting(container, scheme.onSurface, scheme.inverseOnSurface)

    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Row(
            Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            media.art?.let { art ->
                Image(
                    art,
                    contentDescription = stringResource(R.string.media_artwork),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.medium),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    media.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                media.artist?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            FilledIconButton(onClick = onTogglePlay) {
                if (media.isPlaying) {
                    Icon(painterResource(R.drawable.sym_pause_filled), stringResource(R.string.media_pause))
                } else {
                    Icon(painterResource(R.drawable.sym_play_arrow_filled), stringResource(R.string.media_play))
                }
            }
            if (media.canSkipNext) {
                IconButton(onClick = onSkipNext) {
                    Icon(painterResource(R.drawable.sym_skip_next_filled), stringResource(R.string.media_next))
                }
            }
        }
    }
}

/** Whichever of [first] and [second] stands out more against [background]. */
private fun mostContrasting(background: Color, first: Color, second: Color): Color {
    val luminance = background.luminance()
    return if (abs(first.luminance() - luminance) >= abs(second.luminance() - luminance)) first else second
}

/** How far the card moves from the surface colour towards the artwork's average colour. */
private const val ART_TINT = 0.45f
