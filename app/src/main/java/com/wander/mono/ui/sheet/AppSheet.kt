package com.wander.mono.ui.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.ui.components.ActionButtonGroup
import com.wander.mono.ui.components.ActionEmphasis
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.MenuAction
import com.wander.mono.ui.components.MonoSheet
import com.wander.mono.ui.components.SheetDismiss

/**
 * Long-press options for one app: its icon, name and today's use, then its actions as one button
 * group (Wanda's context menu): home in or out first, the app's shortcuts and the rest in pairs,
 * a headphones toggle, the moves as icons, uninstall at the foot.
 */
@Composable
fun AppSheet(
    state: AppSheetState,
    icons: AppIcons,
    usageToday: SheetUsage,
    onDismiss: () -> Unit,
    onShortcut: (AppShortcut) -> Unit,
    onAddToHome: (AppKey) -> Unit,
    onRemoveFromHome: (AppKey) -> Unit,
    onMove: (AppKey, Int) -> Unit,
    onSetHidden: (AppKey, Boolean) -> Unit,
    onSetLiftsWithHeadphones: (AppKey, Boolean) -> Unit,
    onRename: (AppKey) -> Unit,
    onAppInfo: (AppKey) -> Unit,
    onUninstall: (AppKey) -> Unit,
) {
    val key = state.app.key
    MonoSheet(onDismissRequest = onDismiss) { animatedDismiss ->
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            SheetHeader(
                title = state.app.label,
                leading = { AppIcon(key, icons, size = 48.dp) },
                supporting = if (usageToday != SheetUsage.Hidden) {
                    { UsageSlot(usageToday) }
                } else {
                    null
                },
            )
            ActionButtonGroup(
                appActions(
                    state,
                    animatedDismiss,
                    onShortcut,
                    onAddToHome,
                    onRemoveFromHome,
                    onMove,
                    onSetHidden,
                    onSetLiftsWithHeadphones,
                    onRename,
                    onAppInfo,
                    onUninstall,
                ),
                Modifier.padding(top = 8.dp),
            )
        }
    }
}

/**
 * The sheet's buttons. Each slides the sheet away after it runs, except the moves and the
 * headphones toggle, which keep it open to be seen changing, and rename, which runs after: its
 * caller drops the sheet at once to open the rename dialog.
 */
@Suppress("LongParameterList")
private fun appActions(
    state: AppSheetState,
    dismiss: SheetDismiss,
    onShortcut: (AppShortcut) -> Unit,
    onAddToHome: (AppKey) -> Unit,
    onRemoveFromHome: (AppKey) -> Unit,
    onMove: (AppKey, Int) -> Unit,
    onSetHidden: (AppKey, Boolean) -> Unit,
    onSetLiftsWithHeadphones: (AppKey, Boolean) -> Unit,
    onRename: (AppKey) -> Unit,
    onAppInfo: (AppKey) -> Unit,
    onUninstall: (AppKey) -> Unit,
): List<MenuAction> {
    val key = state.app.key
    return buildList {
        if (state.isFavorite) {
            add(MenuAction(R.drawable.sym_remove_circle, R.string.sheet_remove_from_home, ActionEmphasis.Primary) {
                onRemoveFromHome(key)
            }.thenDismiss(dismiss))
        } else if (state.canGoOnHome) {
            add(MenuAction(R.drawable.sym_keep, R.string.sheet_add_to_home, ActionEmphasis.Primary) {
                onAddToHome(key)
            }.thenDismiss(dismiss))
        }
        add(
            MenuAction(R.drawable.sym_info, R.string.sheet_app_info, ActionEmphasis.Secondary) { onAppInfo(key) }
                .thenDismiss(dismiss),
        )
        state.shortcuts.forEach { shortcut ->
            add(MenuAction(R.drawable.sym_arrow_outward, R.string.sheet_shortcut, labelArg = shortcut.label) {
                onShortcut(shortcut)
            }.thenDismiss(dismiss))
        }
        add(MenuAction(R.drawable.sym_edit, R.string.sheet_rename) { onRename(key) }.afterDismiss(dismiss))
        val hideLabel = if (state.isHidden) R.string.sheet_unhide else R.string.sheet_hide
        add(MenuAction(R.drawable.sym_visibility, hideLabel) { onSetHidden(key, !state.isHidden) }.thenDismiss(dismiss))
        state.liftsWithHeadphones?.let { lifts ->
            add(MenuAction(R.drawable.sym_headphones, R.string.sheet_headphones_top, selected = lifts) {
                onSetLiftsWithHeadphones(key, !lifts)
            })
        }
        if (state.canMoveUp) {
            add(MenuAction(R.drawable.sym_keyboard_arrow_up, R.string.sheet_move_up, ActionEmphasis.Icon) {
                onMove(key, -1)
            })
        }
        if (state.canMoveDown) {
            add(MenuAction(R.drawable.sym_keyboard_arrow_down, R.string.sheet_move_down, ActionEmphasis.Icon) {
                onMove(key, 1)
            })
        }
        add(
            MenuAction(R.drawable.sym_delete, R.string.sheet_uninstall, ActionEmphasis.Danger) { onUninstall(key) }
                .thenDismiss(dismiss),
        )
    }
}
