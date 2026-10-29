package com.wander.mono.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.media.PlayingMedia

/** The play/pause pill, with the confirming haptic the spec gives transport actions. */
@Composable
internal fun PlayPauseButton(isPlaying: Boolean, onTogglePlay: () -> Unit, modifier: Modifier = Modifier) {
    val haptics = LocalHapticFeedback.current
    FilledIconButton(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            onTogglePlay()
        },
        modifier = modifier.size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide)),
        shapes = IconButtonDefaults.shapes(),
    ) {
        if (isPlaying) {
            Icon(painterResource(R.drawable.sym_pause_filled), stringResource(R.string.media_pause))
        } else {
            Icon(painterResource(R.drawable.sym_play_arrow_filled), stringResource(R.string.media_play))
        }
    }
}

/** Previous, play/pause and next, shown when the card is expanded. Skips the session cannot do are left out. */
@Composable
internal fun MediaTransport(
    media: PlayingMedia,
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
            IconButton(onClick = onSkipPrevious, shapes = IconButtonDefaults.shapes()) {
                Icon(painterResource(R.drawable.sym_skip_previous_filled), stringResource(R.string.media_previous))
            }
        }
        PlayPauseButton(media.isPlaying, onTogglePlay)
        if (media.canSkipNext) {
            IconButton(onClick = onSkipNext, shapes = IconButtonDefaults.shapes()) {
                Icon(painterResource(R.drawable.sym_skip_next_filled), stringResource(R.string.media_next))
            }
        }
    }
}

private val TRANSPORT_SPACING = 16.dp
