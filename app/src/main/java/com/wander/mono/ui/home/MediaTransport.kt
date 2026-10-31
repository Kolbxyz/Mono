package com.wander.mono.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.ui.components.instantPress
import com.wander.mono.ui.components.rememberMonoHaptics
import com.wander.mono.ui.home.media.rememberConnectedShape
import com.wander.mono.ui.home.media.rememberPlayPauseShape

/**
 * Play/pause, filled with the card's content colour and its icon cut out in the card's [tint], so
 * it reads on any artwork. Its shape says the state (pill paused, rounded square playing, tighter
 * under the finger) and it widens a little while pressed. Fires the confirming haptic the spec
 * gives transport actions.
 */
@Composable
internal fun PlayPauseButton(
    isPlaying: Boolean,
    tint: Color,
    onTogglePlay: () -> Unit,
    size: DpSize,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    TransportButton(
        icon = if (isPlaying) R.drawable.sym_pause_filled else R.drawable.sym_play_arrow_filled,
        label = if (isPlaying) R.string.media_pause else R.string.media_play,
        shape = rememberPlayPauseShape(isPlaying, pressed),
        size = size,
        container = LocalContentColor.current,
        content = tint,
        interaction = interaction,
        onClick = onTogglePlay,
        modifier = modifier,
    )
}

/**
 * Previous, a wide play/pause and next as one connected Expressive button group, centred. The skips
 * are round on their outer side and tight towards play/pause; a skip the session cannot do is left
 * out and play/pause stays centred.
 */
@Composable
internal fun MediaTransport(
    media: PlayingMedia,
    tint: Color,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            ButtonGroupDefaults.ConnectedSpaceBetween,
            Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (media.canSkipPrevious) {
            SkipButton(R.drawable.sym_skip_previous_filled, R.string.media_previous, leading = true, onSkipPrevious)
        }
        PlayPauseButton(media.isPlaying, tint, onTogglePlay, EXPANDED_PLAY)
        if (media.canSkipNext) {
            SkipButton(R.drawable.sym_skip_next_filled, R.string.media_next, leading = false, onSkipNext)
        }
    }
}

/** A tonal skip button, a veil of the card's content colour, connected to play/pause on one side. */
@Composable
private fun SkipButton(@DrawableRes icon: Int, @StringRes label: Int, leading: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val content = LocalContentColor.current
    TransportButton(
        icon = icon,
        label = label,
        shape = rememberConnectedShape(leading, pressed),
        size = SKIP_SIZE,
        container = content.copy(alpha = SKIP_VEIL),
        content = content,
        interaction = interaction,
        onClick = onClick,
    )
}

/** One button of the transport: grows wider under the finger, on the fast spatial spring. */
@Composable
private fun TransportButton(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    shape: Shape,
    size: DpSize,
    container: Color,
    content: Color,
    interaction: MutableInteractionSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberMonoHaptics()
    val pressed by interaction.collectIsPressedAsState()
    val width by animateDpAsState(
        if (pressed) size.width + PRESSED_GROWTH else size.width,
        MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "transport-width",
    )
    Surface(
        onClick = {
            haptics.confirm()
            onClick()
        },
        modifier = modifier.size(width, size.height).instantPress(interaction),
        shape = shape,
        color = container,
        contentColor = content,
        interactionSource = interaction,
    ) {
        // Surface hands its size down as minimum constraints; the box keeps the icon at its own size.
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), stringResource(label))
        }
    }
}

/** Big enough to lead the expanded player, short enough that home keeps room under it. */
private val EXPANDED_PLAY = DpSize(88.dp, 56.dp)
private val SKIP_SIZE = DpSize(56.dp, 56.dp)

/** How much wider a pressed transport button gets. */
private val PRESSED_GROWTH: Dp = 12.dp

/** How strongly the skip buttons' container shows the content colour. */
private const val SKIP_VEIL = 0.16f
