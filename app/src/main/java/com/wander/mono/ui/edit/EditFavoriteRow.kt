package com.wander.mono.ui.edit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.wander.mono.R
import com.wander.mono.data.LiveRowRef
import com.wander.mono.ui.components.AppBadge
import com.wander.mono.ui.components.AppLabel
import com.wander.mono.ui.components.CheckSwitch
import com.wander.mono.ui.home.HomeRow

/**
 * A favourite in edit mode (spec 4.6): drag handle, name, rename and remove, and a switch per
 * live row under it. While [lifted] it rises: larger corners, a shadow, a brighter container
 * and a slight scale, all on the motion scheme's fast spatial spec.
 */
@Composable
internal fun EditFavoriteRow(
    row: HomeRow.App,
    toggles: List<LiveRowToggle>,
    lifted: Boolean,
    onRename: () -> Unit,
    onRemove: () -> Unit,
    onSetLiveRow: (LiveRowRef, Boolean) -> Unit,
    handle: Modifier,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val lift by animateFloatAsState(if (lifted) 1f else 0f, MaterialTheme.motionScheme.fastSpatialSpec())
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer {
                val scale = 1f + LIFT_SCALE * lift
                scaleX = scale
                scaleY = scale
                shadowElevation = LIFT_ELEVATION.toPx() * lift
                shape = RoundedCornerShape(lerp(REST_CORNER, LIFTED_CORNER, lift))
                clip = true
            }
            .background(lerp(scheme.surfaceContainerHigh, scheme.surfaceContainerHighest, lift))
            .padding(end = 4.dp),
    ) {
        Row(Modifier.heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(R.drawable.sym_drag_indicator),
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = handle.padding(12.dp).size(24.dp),
            )
            AppLabel(
                app = row.app,
                badge = AppBadge.None,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRename) {
                Icon(painterResource(R.drawable.sym_edit), contentDescription = stringResource(R.string.edit_rename))
            }
            IconButton(onClick = onRemove) {
                Icon(
                    painterResource(R.drawable.sym_remove_circle),
                    contentDescription = stringResource(R.string.edit_remove),
                    tint = scheme.error,
                )
            }
        }
        toggles.forEach { toggle -> LiveRowSwitch(toggle, onSetLiveRow) }
    }
}

/** One live row of the favourite, indented under its name, with its on/off switch. */
@Composable
private fun LiveRowSwitch(toggle: LiveRowToggle, onSetLiveRow: (LiveRowRef, Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(toggle.shown, role = Role.Switch) { onSetLiveRow(toggle.row, it) }
            .padding(start = 48.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(toggle.row.kind.title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        CheckSwitch(toggle.shown)
    }
}

private const val LIFT_SCALE = 0.03f
private val LIFT_ELEVATION = 8.dp
private val REST_CORNER = 16.dp
private val LIFTED_CORNER = 28.dp
