package com.wander.mono.ui.sheet

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.components.instantPress

/**
 * The sheet's main action: "Open", filled and as wide as the sheet, its corners tightening under
 * the finger. A locked app shows a lock on it. The app's shortcuts sit right under it
 * ([ShortcutRow]).
 */
@Composable
internal fun OpenButton(locked: Boolean, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val corner by animateDpAsState(
        if (pressed) PressedCorner else Height / 2,
        MaterialTheme.motionScheme.fastSpatialSpec(),
    )
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(corner),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        interactionSource = interaction,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(Height)
            .instantPress(interaction),
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Icon(painterResource(if (locked) R.drawable.sym_lock else R.drawable.sym_arrow_outward), null)
            Text(
                stringResource(R.string.action_open),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

private val Height = 64.dp
private val PressedCorner = 18.dp
