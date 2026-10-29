package com.wander.mono.ui.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.apps.PrivateSpaceState
import com.wander.mono.ui.components.AppIcons

/**
 * The private space at the bottom of the drawer: a header with its lock, and its apps under it
 * while it is unlocked. Nothing at all when it is [PrivateSpaceState.Absent].
 */
internal fun LazyListScope.privateSection(
    space: PrivateSpaceState,
    apps: List<DrawerEntry>,
    icons: AppIcons,
    actions: PrivateSpaceActions,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
) {
    if (space !is PrivateSpaceState.Present) return
    item(key = PRIVATE_HEADER_KEY, contentType = PRIVATE_HEADER_KEY) { PrivateHeader(space, actions) }
    if (space.locked) return
    items(apps, key = { "private:${it.app.key.encode()}" }) { entry ->
        DrawerAppRow(
            entry = entry,
            icons = icons,
            onLaunch = { onLaunchApp(entry.app) },
            onLongPress = { onLongPress(entry.app) },
        )
    }
}

@Composable
private fun PrivateHeader(space: PrivateSpaceState.Present, actions: PrivateSpaceActions) {
    Column(Modifier.padding(top = 16.dp)) {
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        Row(
            Modifier.fillMaxWidth().heightIn(min = MIN_ROW_HEIGHT).padding(start = 16.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).semantics(mergeDescendants = true) { heading() }) {
                Text(stringResource(R.string.drawer_private), style = MaterialTheme.typography.titleMedium)
                if (space.locked) {
                    Text(
                        stringResource(R.string.drawer_private_locked),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (space.canOpenSettings) {
                IconButton(onClick = actions.onOpenSettings) {
                    Icon(
                        painterResource(R.drawable.sym_settings),
                        contentDescription = stringResource(R.string.drawer_private_settings),
                    )
                }
            }
            // An action, not a toggle: TalkBack reads what tapping does ("Unlock private space").
            FilledTonalIconButton(onClick = { actions.onSetLocked(!space.locked) }) {
                Icon(
                    painterResource(if (space.locked) R.drawable.sym_lock else R.drawable.sym_lock_open),
                    contentDescription = stringResource(
                        if (space.locked) R.string.drawer_private_unlock else R.string.drawer_private_lock,
                    ),
                )
            }
        }
    }
}

private const val PRIVATE_HEADER_KEY = "private-header"
