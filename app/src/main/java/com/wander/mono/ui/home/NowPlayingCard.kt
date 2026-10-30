package com.wander.mono.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.ui.components.PressMorph
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.home.media.NowPlayingCollapsed
import com.wander.mono.ui.home.media.NowPlayingExpanded
import kotlin.math.abs

/**
 * The playing media, inline under its app and tinted by the artwork. Collapsed it is one balanced
 * line (artwork, title and artist, play/pause) over a thin wavy progress line. A tap expands it to
 * a centred player: large artwork, the wavy seek bar, the connected transport, the output device
 * and what plays next. A horizontal swipe skips either way.
 *
 * Both states are one column with no spacing of its own: each state carries its spacing inside its
 * own [AnimatedVisibility], and both resize on the same spatial spec as the card's corners, so the
 * card's height moves as one and leaves no gap behind while it closes.
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
    // Chosen for the tint it goes to and eased alongside it, so the text never flips in one frame.
    val content by animateColorAsState(
        mostContrasting(tint, scheme.onSurface, scheme.inverseOnSurface),
        motion.defaultEffectsSpec(),
        label = "art-content",
    )
    val corner by animateDpAsState(
        if (expanded) EXPANDED_CORNER else COLLAPSED_CORNER,
        motion.defaultSpatialSpec(),
        label = "card-corner",
    )
    val mediaActions = mediaActions(media, onTogglePlay, onSkipNext, onSkipPrevious)
    val interaction = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = HomeCardMargin)
            .skipSwipe(media.canSkipNext, media.canSkipPrevious) {
                when (it) {
                    SkipDirection.Next -> onSkipNext()
                    SkipDirection.Previous -> onSkipPrevious()
                }
            }
            .expressivePress(interaction, PressMorph.squarer(corner)),
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Column(
            Modifier
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(),
                    onClickLabel = stringResource(if (expanded) R.string.media_collapse else R.string.media_expand),
                ) { expanded = !expanded }
                .semantics { customActions = mediaActions }
                .padding(HomeCardInset),
        ) {
            CardState(visible = !expanded) { NowPlayingCollapsed(media, container, onTogglePlay) }
            CardState(visible = expanded) {
                NowPlayingExpanded(
                    media = media,
                    tint = container,
                    onTogglePlay = onTogglePlay,
                    onSkipNext = onSkipNext,
                    onSkipPrevious = onSkipPrevious,
                    onSeek = onSeek,
                    onPlayQueueItem = onPlayQueueItem,
                    onOpenOutput = onOpenOutput,
                )
            }
        }
    }
}

/**
 * One of the card's two states, growing from and shrinking to its top on the default spatial spec
 * (the one the corners use), so the outgoing state's height hands over to the incoming one's.
 */
@Composable
private fun CardState(visible: Boolean, content: @Composable AnimatedVisibilityScope.() -> Unit) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(motion.defaultSpatialSpec(), expandFrom = Alignment.Top) +
            fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.defaultSpatialSpec(), shrinkTowards = Alignment.Top) +
            fadeOut(motion.fastEffectsSpec()),
        content = content,
    )
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

/** The card's corners grow with it, `shapes.large` to `extraLarge` (spec 5.3). */
private val COLLAPSED_CORNER = HomeCardCorner
private val EXPANDED_CORNER = 36.dp
