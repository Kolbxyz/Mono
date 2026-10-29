package com.wander.mono.ui.edit

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R

/** Edit mode's floating toolbar, in the thumb zone: add an app, settings, and Done. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditToolbar(onDone: () -> Unit, onAddApp: () -> Unit, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    val editingLabel = stringResource(R.string.state_editing)
    HorizontalFloatingToolbar(
        expanded = true,
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .padding(bottom = 16.dp)
            .semantics { paneTitle = editingLabel },
        trailingContent = {
            FilledTonalButton(onClick = onDone, modifier = Modifier.padding(start = 8.dp)) {
                Icon(painterResource(R.drawable.sym_check), contentDescription = null)
                Text(stringResource(R.string.edit_done), Modifier.padding(start = 8.dp))
            }
        },
    ) {
        IconButton(onClick = onAddApp) {
            Icon(painterResource(R.drawable.sym_add), contentDescription = stringResource(R.string.edit_add_app))
        }
        IconButton(onClick = onOpenSettings) {
            Icon(painterResource(R.drawable.sym_settings), contentDescription = stringResource(R.string.edit_settings))
        }
    }
}
