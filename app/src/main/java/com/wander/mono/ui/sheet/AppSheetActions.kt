package com.wander.mono.ui.sheet

import com.wander.mono.R
import com.wander.mono.core.apps.AppKey
import com.wander.mono.ui.components.ActionEmphasis
import com.wander.mono.ui.components.MenuAction
import com.wander.mono.ui.components.SheetDismiss

/** The app sheet's rows of icon buttons, by meaning: the app itself, its place on home, its switches. */
internal const val ROW_APP = 0
internal const val ROW_HOME = 1
internal const val ROW_SWITCHES = 2

/**
 * The sheet's buttons under "Open": home in or out, App info, rename, hide and uninstall (last, in
 * the error colour) as the app's row; [more] adds the favourite's home row (folder, swipe left,
 * daily limit) and its notifications switch; the headphones and lock switches close the switches
 * row. Each slides the sheet away after it runs, except the switches, which keep it open to be seen
 * changing, and rename, which runs after: its caller drops the sheet at once to open the rename
 * dialog.
 */
@Suppress("LongParameterList")
internal fun appActions(
    state: AppSheetState,
    dismiss: SheetDismiss,
    onAddToHome: (AppKey) -> Unit,
    onRemoveFromHome: (AppKey) -> Unit,
    onSetHidden: (AppKey, Boolean) -> Unit,
    onSetLiftsWithHeadphones: (AppKey, Boolean) -> Unit,
    onRename: (AppKey) -> Unit,
    onAppInfo: (AppKey) -> Unit,
    onUninstall: (AppKey) -> Unit,
    onSetLocked: (AppKey, Boolean) -> Unit,
    more: List<MenuAction>,
): List<MenuAction> {
    val key = state.app.key
    return buildList {
        if (state.isFavorite) {
            add(
                MenuAction(
                    R.drawable.sym_remove_circle,
                    R.string.sheet_remove_from_home,
                    group = ROW_APP,
                    shortLabel = R.string.sheet_short_remove_home,
                ) { onRemoveFromHome(key) }.thenDismiss(dismiss),
            )
        } else if (state.canGoOnHome) {
            add(
                MenuAction(
                    R.drawable.sym_keep,
                    R.string.sheet_add_to_home,
                    group = ROW_APP,
                    shortLabel = R.string.sheet_short_add_home,
                ) { onAddToHome(key) }.thenDismiss(dismiss),
            )
        }
        add(
            MenuAction(
                R.drawable.sym_info,
                R.string.sheet_app_info,
                group = ROW_APP,
                shortLabel = R.string.sheet_short_app_info,
            ) { onAppInfo(key) }.thenDismiss(dismiss),
        )
        add(
            MenuAction(R.drawable.sym_edit, R.string.sheet_rename, group = ROW_APP) { onRename(key) }
                .afterDismiss(dismiss),
        )
        val hideLabel = if (state.isHidden) R.string.sheet_unhide else R.string.sheet_hide
        val hideShort = if (state.isHidden) R.string.sheet_short_unhide else R.string.sheet_short_hide
        add(
            MenuAction(R.drawable.sym_visibility, hideLabel, group = ROW_APP, shortLabel = hideShort) {
                onSetHidden(key, !state.isHidden)
            }.thenDismiss(dismiss),
        )
        add(
            MenuAction(R.drawable.sym_delete, R.string.sheet_uninstall, ActionEmphasis.Danger, group = ROW_APP) {
                onUninstall(key)
            }.thenDismiss(dismiss),
        )
        addAll(more)
        state.liftsWithHeadphones?.let { lifts ->
            add(
                MenuAction(
                    R.drawable.sym_headphones,
                    R.string.sheet_headphones_top,
                    selected = lifts,
                    group = ROW_SWITCHES,
                    shortLabel = R.string.sheet_short_headphones,
                ) { onSetLiftsWithHeadphones(key, !lifts) },
            )
        }
        add(
            MenuAction(
                R.drawable.sym_lock,
                R.string.sheet_lock,
                selected = state.isLocked,
                group = ROW_SWITCHES,
                shortLabel = R.string.sheet_short_lock,
            ) { onSetLocked(key, !state.isLocked) },
        )
    }
}
