package com.wander.mono.ui.home.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.wander.mono.R
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.ui.theme.textAlpha

/**
 * The artwork at [size], its [corner] by default a share of its size so it keeps its look at any size. Without
 * artwork the slot stays, as a veil of the content colour holding a note glyph: the layout keeps
 * its balance and nothing pretends to be a cover.
 */
@Composable
internal fun MediaArt(art: ImageBitmap?, size: Dp, modifier: Modifier = Modifier, corner: Dp = size * ART_CORNER) {
    val shape = RoundedCornerShape(corner)
    if (art != null) {
        Image(
            art,
            contentDescription = stringResource(R.string.media_artwork),
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(shape),
        )
    } else {
        Box(
            modifier.size(size).clip(shape).background(LocalContentColor.current.copy(alpha = PLACEHOLDER_VEIL)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.sym_music_note),
                contentDescription = null,
                modifier = Modifier.size(size * GLYPH),
            )
        }
    }
}

/**
 * Title over artist; [large] for the expanded player, [centred] when they sit on its axis, the
 * title wrapping to at most [titleLines].
 */
@Composable
internal fun MediaTitle(
    media: PlayingMedia,
    large: Boolean,
    modifier: Modifier = Modifier,
    centred: Boolean = large,
    titleLines: Int = if (large) 2 else 1,
) {
    val align = if (centred) TextAlign.Center else TextAlign.Start
    Column(modifier, horizontalAlignment = if (centred) Alignment.CenterHorizontally else Alignment.Start) {
        Text(
            media.title,
            style = with(MaterialTheme.typography) { if (large) titleLarge else titleMedium },
            textAlign = align,
            maxLines = titleLines,
            overflow = TextOverflow.Ellipsis,
        )
        media.artist?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalContentColor.current.copy(alpha = textAlpha(ARTIST_ALPHA)),
                textAlign = align,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Artwork corners as a share of its size. */
private const val ART_CORNER = 0.26f

/** The empty artwork slot: a light veil of the content colour, its glyph a share of the slot. */
private const val PLACEHOLDER_VEIL = 0.14f
private const val GLYPH = 0.42f

/** The artist line, dimmer than the title in the same contrasting colour. */
private const val ARTIST_ALPHA = 0.74f
