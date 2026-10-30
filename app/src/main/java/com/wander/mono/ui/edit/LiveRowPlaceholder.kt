package com.wander.mono.ui.edit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.data.LiveRowRef
import com.wander.mono.ui.components.CheckSwitch
import com.wander.mono.ui.components.PressMorph
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.home.HomeCardCorner

/**
 * A live row (now playing, notification preview, calendar) as edit mode shows it: a compact card
 * with its symbol, name and state in place of the live content, the whole card one switch. A
 * hidden row stays here, dimmed, so it can be switched back on.
 */
@Composable
internal fun LiveRowPlaceholder(
    toggle: LiveRowToggle,
    onSetLiveRow: (LiveRowRef, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val kind = toggle.row.kind
    val interaction = remember { MutableInteractionSource() }
    val content by animateFloatAsState(
        if (toggle.shown) 1f else HIDDEN_ALPHA,
        MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "placeholder",
    )
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .expressivePress(interaction, PressMorph.squarer(HomeCardCorner))
            .background(scheme.surfaceContainer)
            .toggleable(toggle.shown, interaction, ripple(), role = Role.Switch) { onSetLiveRow(toggle.row, it) }
            .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            painterResource(kind.icon),
            contentDescription = null,
            tint = scheme.primary,
            modifier = Modifier.size(24.dp).alpha(content),
        )
        Column(Modifier.weight(1f).alpha(content)) {
            Text(
                stringResource(kind.title),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                stringResource(if (toggle.shown) R.string.edit_live_row_shown else R.string.edit_live_row_hidden),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        CheckSwitch(toggle.shown)
    }
}

/** How far a switched-off placeholder fades, so on and off read apart at a glance. */
private const val HIDDEN_ALPHA = 0.5f
