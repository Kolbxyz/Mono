package com.wander.mono.ui.home.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import com.wander.mono.R
import com.wander.mono.core.media.PlayingMedia

/**
 * The swipe is not reachable with TalkBack and the strip's buttons fold away while expanded, so
 * play/pause, the skips and opening the app are also offered as actions on the player itself.
 */
@Composable
internal fun mediaActions(media: PlayingMedia, controls: MediaControls): List<CustomAccessibilityAction> {
    val toggle = stringResource(if (media.isPlaying) R.string.media_pause else R.string.media_play)
    val previous = stringResource(R.string.media_previous)
    val next = stringResource(R.string.media_next)
    val open = stringResource(R.string.media_open_player)
    return buildList {
        add(action(toggle, controls::togglePlayPause))
        if (media.canSkipPrevious) add(action(previous, controls::skipPrevious))
        if (media.canSkipNext) add(action(next, controls::skipNext))
        add(action(open, controls::openPlayer))
    }
}

private fun action(label: String, run: () -> Unit) = CustomAccessibilityAction(label) {
    run()
    true
}
