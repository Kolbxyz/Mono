package com.wander.mono.ui.home.media

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.ui.components.rememberMonoHaptics
import com.wander.mono.ui.home.PlayPauseButton
import com.wander.mono.ui.theme.isLargeFontScale

/**
 * The strip's transport: previous, play/pause (filled, its icon cut out in [tint]) and next, small.
 * A skip the session cannot do is left out; above 1.5× font scale only play/pause stays, so the
 * title keeps its width.
 */
@Composable
internal fun CompactTransport(
    media: PlayingMedia,
    tint: Color,
    controls: MediaControls,
    modifier: Modifier = Modifier,
) {
    val skips = !isLargeFontScale()
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (skips && media.canSkipPrevious) {
            SmallSkip(R.drawable.sym_skip_previous_filled, R.string.media_previous, controls::skipPrevious)
        }
        PlayPauseButton(media.isPlaying, tint, controls::togglePlayPause, size = STRIP_PLAY)
        if (skips && media.canSkipNext) {
            SmallSkip(R.drawable.sym_skip_next_filled, R.string.media_next, controls::skipNext)
        }
    }
}

@Composable
private fun SmallSkip(@DrawableRes icon: Int, @StringRes label: Int, onClick: () -> Unit) {
    val haptics = rememberMonoHaptics()
    IconButton(
        onClick = {
            haptics.confirm()
            onClick()
        },
        modifier = Modifier.size(SKIP_TOUCH),
    ) {
        Icon(painterResource(icon), stringResource(label), Modifier.size(SKIP_ICON))
    }
}

/** As tall as the artwork thumb beside it, a little wider, so it reads as the main button. */
private val STRIP_PLAY = DpSize(48.dp, 44.dp)
private val SKIP_TOUCH = 40.dp
private val SKIP_ICON = 22.dp
