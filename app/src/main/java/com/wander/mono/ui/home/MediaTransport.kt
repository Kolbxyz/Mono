package com.wander.mono.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.media.PlayingMedia

/**
 * Play/pause, filled with the card's content colour and its icon cut out in the card's [tint], so it
 * reads on any artwork. Its corners say
 * the state, as in Wanda: a round pill while paused, a squarer one while playing, tighter still
 * under the finger. Fires the confirming haptic the spec gives transport actions.
 */
@Composable
internal fun PlayPauseButton(
    isPlaying: Boolean,
    tint: Color,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier,
    size: DpSize = COLLAPSED_PLAY,
) {
    val haptics = LocalHapticFeedback.current
    val content = LocalContentColor.current
    val corner by animateDpAsState(
        if (isPlaying) size.height * PLAYING_CORNER else size.height / 2,
        MaterialTheme.motionScheme.fastSpatialSpec(),
    )
    FilledIconButton(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            onTogglePlay()
        },
        modifier = modifier.size(size),
        shapes = IconButtonShapes(RoundedCornerShape(corner), RoundedCornerShape(size.height * PRESSED_CORNER)),
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = content, contentColor = tint),
    ) {
        if (isPlaying) {
            Icon(painterResource(R.drawable.sym_pause_filled), stringResource(R.string.media_pause))
        } else {
            Icon(painterResource(R.drawable.sym_play_arrow_filled), stringResource(R.string.media_play))
        }
    }
}

/** Previous, a wide play/pause and next, shown when the card is expanded. Skips the session cannot do are left out. */
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
        horizontalArrangement = Arrangement.spacedBy(TRANSPORT_SPACING, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (media.canSkipPrevious) {
            SkipButton(R.drawable.sym_skip_previous_filled, R.string.media_previous, onSkipPrevious)
        }
        PlayPauseButton(media.isPlaying, tint, onTogglePlay, size = EXPANDED_PLAY)
        if (media.canSkipNext) {
            SkipButton(R.drawable.sym_skip_next_filled, R.string.media_next, onSkipNext)
        }
    }
}

/** A round tonal skip button, a veil of the card's content colour. */
@Composable
private fun SkipButton(icon: Int, label: Int, onClick: () -> Unit) {
    val content = LocalContentColor.current
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(SKIP_SIZE),
        shapes = IconButtonShapes(CircleShape, RoundedCornerShape(SKIP_SIZE * PRESSED_CORNER)),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = content.copy(alpha = SKIP_VEIL),
            contentColor = content,
        ),
    ) {
        Icon(painterResource(icon), stringResource(label))
    }
}

private val COLLAPSED_PLAY = DpSize(64.dp, 48.dp)
private val EXPANDED_PLAY = DpSize(96.dp, 64.dp)
private val SKIP_SIZE = 56.dp
private val TRANSPORT_SPACING = 12.dp

/** Corner radius as a share of the height: playing, and under the finger. */
private const val PLAYING_CORNER = 0.3f
private const val PRESSED_CORNER = 0.2f

/** How strongly the skip buttons' container shows the content colour. */
private const val SKIP_VEIL = 0.16f

