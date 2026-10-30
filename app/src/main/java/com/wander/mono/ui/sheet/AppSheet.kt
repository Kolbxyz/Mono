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
import com.wander.mono.ui.components.rememberCredentialPrompt

/**
 * Long-press options for one app: its icon, name (a small lock after it while locked) and today's
 * use, then "Open" as a split button with the app's shortcuts in its menu, then its actions as one
 * button group (Wanda's context menu): home in or out first, the rest in pairs, the headphones and
 * lock toggles, the moves as icons, uninstall at the foot.
 */
@Composable
fun AppSheet(
    state: AppSheetState,
    icons: AppIcons,
    usageToday: SheetUsage,
    onDismiss: () -> Unit,
    onOpen: (AppKey) -> Unit,
    onShortcut: (AppShortcut) -> Unit,
    onAddToHome: (AppKey) -> Unit,
    onRemoveFromHome: (AppKey) -> Unit,
    onMove: (AppKey, Int) -> Unit,
    onSetHidden: (AppKey, Boolean) -> Unit,
    onSetLiftsWithHeadphones: (AppKey, Boolean) -> Unit,
    onRename: (AppKey) -> Unit,
    onAppInfo: (AppKey) -> Unit,
    onUninstall: (AppKey) -> Unit,
    onSetLocked: (AppKey, Boolean) -> Unit,
) {
    val key = state.app.key
    // Anyone holding the phone could otherwise lift the lock from here: removing it asks first.
    val unlockForGood = rememberCredentialPrompt(
        R.string.app_lock_remove_title,
        onSuccess = { onSetLocked(key, false) },
        // Without a screen lock the lock guards nothing, and would keep the app shut for good.
        onNoScreenLock = { onSetLocked(key, false) },
    )
    val setLocked: (AppKey, Boolean) -> Unit = { app, lock ->
        if (lock) onSetLocked(app, true) else unlockForGood()
    }
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
                badge = { LockBadge(state.isLocked) },
            )
            OpenSplitButton(
                locked = state.isLocked,
                shortcuts = state.shortcuts,
                onOpen = {
                    onOpen(key)
                    animatedDismiss()
                },
                onShortcut = { shortcut ->
                    onShortcut(shortcut)
                    animatedDismiss()
                },
                modifier = Modifier.padding(top = 8.dp),
            )
            ActionButtonGroup(
                appActions(
                    state,
                    animatedDismiss,
                    onAddToHome,
                    onRemoveFromHome,
                    onMove,
                    onSetHidden,
                    onSetLiftsWithHeadphones,
                    onRename,
                    onAppInfo,
                    onUninstall,
                    setLocked,
                ),
                Modifier.padding(top = 8.dp),
            )
        }
    }
}

/**
 * The sheet's buttons. Each slides the sheet away after it runs, except the moves and the
 * headphones and lock toggles, which keep it open to be seen changing, and rename, which runs
 * after: its caller drops the sheet at once to open the rename dialog.
 */
@Suppress("LongParameterList")
private fun appActions(
    state: AppSheetState,
    dismiss: SheetDismiss,
    onAddToHome: (AppKey) -> Unit,
    onRemoveFromHome: (AppKey) -> Unit,
    onMove: (AppKey, Int) -> Unit,
    onSetHidden: (AppKey, Boolean) -> Unit,
    onSetLiftsWithHeadphones: (AppKey, Boolean) -> Unit,
    onRename: (AppKey) -> Unit,
    onAppInfo: (AppKey) -> Unit,
    onUninstall: (AppKey) -> Unit,
    onSetLocked: (AppKey, Boolean) -> Unit,
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
        add(MenuAction(R.drawable.sym_edit, R.string.sheet_rename) { onRename(key) }.afterDismiss(dismiss))
        val hideLabel = if (state.isHidden) R.string.sheet_unhide else R.string.sheet_hide
        add(MenuAction(R.drawable.sym_visibility, hideLabel) { onSetHidden(key, !state.isHidden) }.thenDismiss(dismiss))
        state.liftsWithHeadphones?.let { lifts ->
            add(MenuAction(R.drawable.sym_headphones, R.string.sheet_headphones_top, selected = lifts) {
                onSetLiftsWithHeadphones(key, !lifts)
            })
        }
        add(MenuAction(R.drawable.sym_lock, R.string.sheet_lock, selected = state.isLocked) {
            onSetLocked(key, !state.isLocked)
        })
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
