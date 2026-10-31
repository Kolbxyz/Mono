package com.wander.mono.ui.edit

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.data.LiveRowRef
import com.wander.mono.ui.components.AppBadge
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.AppLabel
import com.wander.mono.ui.home.HomeRow

/**
 * A favourite in edit mode (spec 4.6): drag handle, icon (none in text-only mode), name, rename
 * and remove, then a compact placeholder card for each of its live rows, where that row shows on
 * home, with its on/off switch. The placeholders ride with their app: while [lifted] the whole item rises, the
 * favourite's card gaining larger corners, a shadow and a brighter container.
 */
@Composable
internal fun EditFavoriteRow(
    row: HomeRow.App,
    icons: AppIcons,
    toggles: List<LiveRowToggle>,
    lifted: Boolean,
    onRename: () -> Unit,
    onRemove: () -> Unit,
    onSetLiveRow: (LiveRowRef, Boolean) -> Unit,
    handle: Modifier,
    modifier: Modifier = Modifier,
) {
    val lift = animateLift(lifted)
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .liftScale(lift),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .editCard(lift)
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DragHandle(handle)
            // The icon home shows, so the row is recognised at a look while moving it.
            AppIcon(row.app.key, icons, Modifier.padding(end = 12.dp), size = EDIT_ICON_SIZE)
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
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        // A live row becoming available slides its placeholder in. Only this part resizes: the
        // size animation clips, and the card above draws its lift shadow past its bounds.
        Column(Modifier.animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec())) {
            toggles.forEach { toggle ->
                LiveRowPlaceholder(toggle, onSetLiveRow, Modifier.padding(start = PLACEHOLDER_INDENT, top = 4.dp))
            }
        }
    }
}

/** Placeholders sit under their app, indented as far as the drag handle, so they read as its rows. */
private val PLACEHOLDER_INDENT = 24.dp

/** Smaller than home's icon: the card also holds a handle and two buttons. */
private val EDIT_ICON_SIZE = 32.dp
