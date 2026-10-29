package com.wander.mono.ui.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.ui.components.ReadableLayout

/** Long-press options for one app: its shortcuts, then home list, visibility, app info and uninstall. */
@Composable
fun AppSheet(
    state: AppSheetState,
    usageToday: Long?,
    onDismiss: () -> Unit,
    onShortcut: (AppShortcut) -> Unit,
    onAddToHome: (AppKey) -> Unit,
    onRemoveFromHome: (AppKey) -> Unit,
    onMove: (AppKey, Int) -> Unit,
    onSetHidden: (AppKey, Boolean) -> Unit,
    onRename: (AppKey) -> Unit,
    onAppInfo: (AppKey) -> Unit,
    onUninstall: (AppKey) -> Unit,
) {
    val key = state.app.key
    ModalBottomSheet(onDismissRequest = onDismiss, sheetMaxWidth = ReadableLayout.MaxColumnWidth) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
            Text(
                state.app.label,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).semantics { heading() },
            )
            usageToday?.let { UsageLine(it) }
            state.shortcuts.forEach { shortcut -> SheetAction(shortcut.label) { onShortcut(shortcut) } }
            if (state.shortcuts.isNotEmpty()) HorizontalDivider(Modifier.padding(vertical = 8.dp))
            if (state.isFavorite) {
                if (state.canMoveUp) SheetAction(stringResource(R.string.sheet_move_up)) { onMove(key, -1) }
                if (state.canMoveDown) SheetAction(stringResource(R.string.sheet_move_down)) { onMove(key, 1) }
                SheetAction(stringResource(R.string.sheet_remove_from_home)) { onRemoveFromHome(key) }
            } else {
                SheetAction(stringResource(R.string.sheet_add_to_home)) { onAddToHome(key) }
            }
            SheetAction(stringResource(R.string.sheet_rename)) { onRename(key) }
            if (state.isHidden) {
                SheetAction(stringResource(R.string.sheet_unhide)) { onSetHidden(key, false) }
            } else {
                SheetAction(stringResource(R.string.sheet_hide)) { onSetHidden(key, true) }
            }
            SheetAction(stringResource(R.string.sheet_app_info)) { onAppInfo(key) }
            SheetAction(stringResource(R.string.sheet_uninstall)) { onUninstall(key) }
        }
    }
}

@Composable
internal fun SheetAction(label: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    ) {
        Text(label)
    }
}
