package com.wander.mono.ui.home

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.wander.mono.R
import com.wander.mono.core.media.AudioOutput

/** Where the media is heard; a tap opens the system output switcher for the playing app. */
@Composable
internal fun OutputChip(output: AudioOutput, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val content = LocalContentColor.current
    val switchLabel = stringResource(R.string.media_output_switch)
    val name = when (output) {
        is AudioOutput.Local -> output.name
        AudioOutput.Remote -> stringResource(R.string.media_output_remote)
    }
    AssistChip(
        onClick = onClick,
        label = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { Icon(painterResource(R.drawable.sym_devices), contentDescription = null) },
        modifier = modifier.semantics {
            onClick(switchLabel) {
                onClick()
                true
            }
        },
        // The card takes its tint from the artwork, so the chip follows its contrasting content colour.
        colors = AssistChipDefaults.assistChipColors(labelColor = content, leadingIconContentColor = content),
        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = content.copy(alpha = BORDER_ALPHA)),
    )
}

private const val BORDER_ALPHA = 0.4f
