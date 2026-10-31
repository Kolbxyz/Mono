package com.wander.mono.ui.home

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.wander.mono.R
import com.wander.mono.core.media.AudioOutput
import com.wander.mono.ui.home.media.PillIconSize
import com.wander.mono.ui.home.media.PillLabel
import com.wander.mono.ui.home.media.PlayerPill

/**
 * Where the media is heard, as a pill beside the app chip in the expanded player's header; a tap
 * opens the system output switcher for the playing app.
 */
@Composable
internal fun OutputChip(output: AudioOutput, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val name = when (output) {
        is AudioOutput.Local -> output.name
        AudioOutput.Remote -> stringResource(R.string.media_output_remote)
    }
    PlayerPill(onClickLabel = stringResource(R.string.media_output_switch), onClick = onClick, modifier = modifier) {
        Icon(painterResource(R.drawable.sym_devices), contentDescription = null, modifier = Modifier.size(PillIconSize))
        PillLabel(name)
    }
}
