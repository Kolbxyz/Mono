package com.wander.mono.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.ui.theme.isLargeFontScale
import kotlin.math.abs

/**
 * The playing media, inline under its app and tinted by the artwork. Collapsed it is one calm line:
 * artwork, title, artist, play/pause and the wavy progress. A tap expands it to bigger artwork and
 * previous / play / next, the output device, a draggable bar to seek and what plays next; a
 * horizontal swipe skips either way. Above 1.5× font scale the collapsed play/pause moves under the
 * text so the title keeps its width.
 */
@Composable
fun NowPlayingCard(
    media: PlayingMedia,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (positionMs: Long) -> Unit,
    onPlayQueueItem: (id: Long) -> Unit,
    onOpenOutput: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val scheme = MaterialTheme.colorScheme
    var expanded by rememberSaveable { mutableStateOf(false) }
    val tint = media.artColor?.let { lerp(scheme.surfaceContainerHigh, Color(it), ART_TINT) }
        ?: scheme.surfaceContainerHigh
    val container by animateColorAsState(tint, motion.defaultEffectsSpec(), label = "art-tint")
    val content = mostContrasting(container, scheme.onSurface, scheme.inverseOnSurface)
    val artSize by animateDpAsState(if (expanded) ART_EXPANDED else ART_COLLAPSED, motion.defaultSpatialSpec())
    val mediaActions = mediaActions(media, onTogglePlay, onSkipNext, onSkipPrevious)
    val stacked = isLargeFontScale()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .skipSwipe(media.canSkipNext, media.canSkipPrevious) {
                when (it) {
                    SkipDirection.Next -> onSkipNext()
                    SkipDirection.Previous -> onSkipPrevious()
                }
            },
        shape = if (expanded) MaterialTheme.shapes.extraLarge else MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Column(
            Modifier
                .clickable(
                    onClickLabel = stringResource(if (expanded) R.string.media_collapse else R.string.media_expand),
                ) { expanded = !expanded }
                .semantics { customActions = mediaActions }
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                media.art?.let { art ->
                    Image(
                        art,
                        contentDescription = stringResource(R.string.media_artwork),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(artSize).clip(MaterialTheme.shapes.medium),
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        media.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = if (expanded) 2 else 1,
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
                if (!expanded && !stacked) PlayPauseButton(media.isPlaying, onTogglePlay)
            }
            if (!expanded && stacked) {
                PlayPauseButton(media.isPlaying, onTogglePlay, Modifier.align(Alignment.End))
            }
            ExpandedPart(expanded) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MediaTransport(media, onTogglePlay, onSkipNext, onSkipPrevious)
                    media.output?.let { OutputChip(it, onOpenOutput) }
                }
            }
            media.progress?.let {
                if (expanded && media.canSeek) {
                    SeekablePlaybackBar(it, color = content, onSeek = onSeek)
                } else {
                    PlaybackBar(it, color = content)
                }
            }
            if (media.upNext.isNotEmpty()) {
                ExpandedPart(expanded) { UpNextList(media.upNext, onPlayQueueItem) }
            }
        }
    }
}

/** Content only the expanded card shows, growing in and out with the card. */
@Composable
private fun ExpandedPart(expanded: Boolean, content: @Composable () -> Unit) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.fastEffectsSpec()),
        exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
    ) {
        content()
    }
}

/**
 * The swipe is not reachable with TalkBack and the transport hides while collapsed, so play/pause
 * and the skips are also offered as actions on the card itself.
 */
@Composable
private fun mediaActions(
    media: PlayingMedia,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
): List<CustomAccessibilityAction> {
    val previous = stringResource(R.string.media_previous)
    val next = stringResource(R.string.media_next)
    val toggle = stringResource(if (media.isPlaying) R.string.media_pause else R.string.media_play)
    return buildList {
        add(
            CustomAccessibilityAction(toggle) {
                onTogglePlay()
                true
            },
        )
        if (media.canSkipPrevious) {
            add(
                CustomAccessibilityAction(previous) {
                    onSkipPrevious()
                    true
                },
            )
        }
        if (media.canSkipNext) {
            add(
                CustomAccessibilityAction(next) {
                    onSkipNext()
                    true
                },
            )
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

private val ART_COLLAPSED = 48.dp
private val ART_EXPANDED = 96.dp
