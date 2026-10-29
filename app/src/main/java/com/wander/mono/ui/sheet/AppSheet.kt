package com.wander.mono.ui.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.ReadableLayout
import com.wander.mono.ui.components.SegmentedColumn

/**
 * Long-press options for one app: its icon, name and today's use; the four actions people reach for
 * as tiles (home, app info, visibility, uninstall); then its shortcuts and the rest as segmented lists.
 */
@Composable
fun AppSheet(
    state: AppSheetState,
    icons: AppIcons,
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetMaxWidth = ReadableLayout.MaxColumnWidth,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                Modifier.padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(key, icons, size = 56.dp)
                Column {
                    Text(
                        state.app.label,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.semantics { heading() },
                    )
                    usageToday?.let { UsageLine(it) }
                }
            }
            SheetTileRow {
                if (state.isFavorite) {
                    SheetTile(R.drawable.sym_remove_circle, stringResource(R.string.sheet_remove_from_home), {
                        onRemoveFromHome(key)
                    }, emphasis = true)
                } else {
                    SheetTile(R.drawable.sym_keep, stringResource(R.string.sheet_add_to_home), {
                        onAddToHome(key)
                    }, emphasis = true)
                }
                SheetTile(R.drawable.sym_info, stringResource(R.string.sheet_app_info), { onAppInfo(key) })
                val hideLabel = if (state.isHidden) R.string.sheet_unhide else R.string.sheet_hide
                SheetTile(R.drawable.sym_visibility, stringResource(hideLabel), { onSetHidden(key, !state.isHidden) })
                SheetTile(R.drawable.sym_delete, stringResource(R.string.sheet_uninstall), {
                    onUninstall(key)
                }, destructive = true)
            }
            if (state.shortcuts.isNotEmpty()) {
                SegmentedColumn(Modifier.padding(horizontal = 16.dp)) {
                    state.shortcuts.forEach { shortcut -> SheetAction(shortcut.label) { onShortcut(shortcut) } }
                }
            }
            SegmentedColumn(Modifier.padding(horizontal = 16.dp)) {
                SheetAction(stringResource(R.string.sheet_rename), R.drawable.sym_edit) { onRename(key) }
                if (state.isFavorite && state.canMoveUp) {
                    SheetAction(stringResource(R.string.sheet_move_up), R.drawable.sym_keyboard_arrow_up) {
                        onMove(key, -1)
                    }
                }
                if (state.isFavorite && state.canMoveDown) {
                    SheetAction(stringResource(R.string.sheet_move_down), R.drawable.sym_keyboard_arrow_down) {
                        onMove(key, 1)
                    }
                }
            }
        }
    }
}
