package com.wander.mono.ui.home

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.components.expressivePress

/**
 * The way into the drawer from home: a tonal pill in the thumb zone with the search hint, as the
 * drawer opens with its search field. Opaque, never glass, so it reads on any wallpaper (spec 5.4).
 */
@Composable
fun SearchDock(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val label = stringResource(R.string.action_open_drawer)
    Surface(
        onClick = onOpen,
        modifier = modifier
            .fillMaxWidth()
            // Required, not just preferred: whatever runs short above it, the pill keeps its shape.
            .requiredHeight(DOCK_HEIGHT)
            .expressivePress(interaction)
            .clearAndSetSemantics {
                contentDescription = label
                onClick { onOpen().let { true } }
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        interactionSource = interaction,
    ) {
        Row(
            Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.sym_search), contentDescription = null)
            Text(
                stringResource(R.string.drawer_search_hint),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(painterResource(R.drawable.sym_apps), contentDescription = null)
        }
    }
}

private val DOCK_HEIGHT = 56.dp

/**
 * The height home gives the dock, with the 8 dp home sets above and below it, so the player under
 * the clock leaves it this much ([HomePlayerSlot]).
 */
internal val SearchDockRoom = DOCK_HEIGHT + 16.dp
