package com.wander.mono.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.data.settings.BooleanSetting
import com.wander.mono.data.settings.MediaSettings
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.PressMorph
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.home.media.MediaControls
import com.wander.mono.ui.home.media.PlayerBackdrop
import com.wander.mono.ui.home.media.PlayerControls
import com.wander.mono.ui.home.media.PlayerExpansion
import com.wander.mono.ui.home.media.PlayerHead
import com.wander.mono.ui.home.media.ScrubLine
import com.wander.mono.ui.home.media.mediaActions
import com.wander.mono.ui.home.media.playerBounds
import com.wander.mono.ui.settings.SettingsViewModel
import com.wander.mono.ui.settings.collect
import kotlin.math.abs

/**
 * The playing media under the clock, tinted by the artwork. It is one card that morphs between two
 * forms on the motion scheme, never swapping one layout for another: collapsed, a pill-like strip
 * (artwork thumb, title · artist, previous / play / next, a thin progress line that scrubs when
 * dragged); a tap grows the same artwork in place, its corners easing, while the strip's buttons
 * fold away and the full player opens under it (app chip, wavy seek bar, connected transport,
 * output, what plays next) over the blurred artwork. A tap on it again, outside it, back or a
 * scroll of home collapses it ([expansion]). The artwork and the app chip open the player app; a
 * horizontal swipe skips; a long press opens its options ([onLongPress]). [MediaSettings] choose
 * whether it starts expanded, takes the artwork's colour and skips on a swipe. [compact] (a phone
 * on its side) keeps the expanded player short.
 */
@Composable
fun NowPlayingCard(
    player: HomePlayer,
    expansion: PlayerExpansion,
    controls: MediaControls,
    icons: AppIcons,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    settings: SettingsViewModel = viewModel(),
) {
    val media = player.media
    val motion = MaterialTheme.motionScheme
    val scheme = MaterialTheme.colorScheme
    val expanded = expansion.expanded
    val swipeToSkip = settings.value(MediaSettings.SwipeToSkip)
    val artTint = settings.value(MediaSettings.ArtTint)
    val tint = media.artColor?.takeIf { artTint }?.let { lerp(scheme.surfaceContainerHigh, Color(it), ART_TINT) }
        ?: scheme.surfaceContainerHigh
    val container by animateColorAsState(tint, motion.defaultEffectsSpec(), label = "art-tint")
    // Chosen for the tint it goes to and eased alongside it, so the text never flips in one frame.
    val content by animateColorAsState(
        mostContrasting(tint, scheme.onSurface, scheme.inverseOnSurface),
        motion.defaultEffectsSpec(),
        label = "art-content",
    )
    // One value drives the whole morph, so the artwork, corners and backdrop move as one.
    val morph by animateFloatAsState(if (expanded) 1f else 0f, motion.defaultSpatialSpec(), label = "player-morph")
    val settled = morph.coerceIn(0f, 1f)
    val corner = lerp(COLLAPSED_CORNER, EXPANDED_CORNER, settled)
    val actions = mediaActions(media, controls)
    val interaction = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .playerBounds(expansion)
            .then(if (swipeToSkip) Modifier.skipSwipe(media, controls) else Modifier)
            .expressivePress(interaction, PressMorph.squarer(corner)),
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Box {
            if (artTint) PlayerBackdrop(media, container, { morph }, Modifier.matchParentSize())
            Column(
                Modifier
                    .combinedClickable(
                        interactionSource = interaction,
                        indication = ripple(),
                        onClickLabel = stringResource(if (expanded) R.string.media_collapse else R.string.media_expand),
                        onLongClickLabel = stringResource(R.string.media_sheet_open),
                        onLongClick = onLongPress,
                        onClick = expansion::toggle,
                    )
                    .semantics { customActions = actions }
                    // The strip hugs its row; the full player takes the inset home's cards share.
                    .padding(
                        horizontal = lerp(STRIP_INSET_SIDE, HomeCardInset, settled),
                        vertical = lerp(STRIP_INSET_EDGE, HomeCardInset, settled),
                    ),
            ) {
                PlayerHead(player, morph, expanded, compact, container, controls, icons)
                media.progress?.let { progress ->
                    PlayerPart(visible = !expanded) {
                        ScrubLine(
                            progress = progress,
                            onSeek = controls::seekTo.takeIf { media.canSeek },
                            color = content,
                            modifier = Modifier.padding(top = STRIP_GAP),
                        )
                    }
                }
                PlayerPart(visible = expanded) { PlayerControls(media, container, compact, controls) }
            }
        }
    }
}

/**
 * A part only one form shows, growing from and shrinking to its top on the default spatial spec
 * (the one the morph uses), so the card's height moves with the artwork.
 */
@Composable
private fun PlayerPart(visible: Boolean, content: @Composable () -> Unit) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(motion.defaultSpatialSpec(), expandFrom = Alignment.Top) +
            fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.defaultSpatialSpec(), shrinkTowards = Alignment.Top) +
            fadeOut(motion.fastEffectsSpec()),
    ) { content() }
}

/** The swipe to skip, for the directions the session can skip. */
@Composable
private fun Modifier.skipSwipe(media: PlayingMedia, controls: MediaControls): Modifier =
    skipSwipe(media.canSkipNext, media.canSkipPrevious) {
        when (it) {
            SkipDirection.Next -> controls.skipNext()
            SkipDirection.Previous -> controls.skipPrevious()
        }
    }

/** [setting]'s stored value, its default only before the settings file is first read. */
@Composable
private fun SettingsViewModel.value(setting: BooleanSetting): Boolean = collect(setting) ?: setting.default

/** Whichever of [first] and [second] stands out more against [background]. */
private fun mostContrasting(background: Color, first: Color, second: Color): Color {
    val luminance = background.luminance()
    return if (abs(first.luminance() - luminance) >= abs(second.luminance() - luminance)) first else second
}

/** How far the card moves from the surface colour towards the artwork's average colour. */
private const val ART_TINT = 0.45f

/** Collapsed, the corners round the strip into a pill; expanded, `shapes.extraLarge` (spec 5.3). */
private val COLLAPSED_CORNER = 32.dp
private val EXPANDED_CORNER = 28.dp

/** The strip's inset: snug above and below its row, a little more at the sides for the pill's curve. */
private val STRIP_INSET_EDGE = 10.dp
private val STRIP_INSET_SIDE = 12.dp

/** Between the strip's row and its progress line. */
private val STRIP_GAP = 2.dp
