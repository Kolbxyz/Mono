package com.wander.mono.ui.sheet

import com.wander.mono.R
import com.wander.mono.core.apps.AppKey
import com.wander.mono.ui.components.MenuAction
import com.wander.mono.ui.components.SheetDismiss

/**
 * The sheet's actions about home's structure, for a favourite only: its folder (add, or take out),
 * its swipe left and, while the nudge is on and the app is of Mono's own profile (the only one
 * whose time is readable), its daily limit, as the home row; then whether home shows its
 * notifications, first of the switches. Choices open a [page] of their own in the sheet; the
 * notifications switch keeps the sheet open to be seen changing.
 */
@Suppress("LongParameterList")
internal fun structureActions(
    state: AppSheetState,
    structure: AppSheetStructure,
    dismiss: SheetDismiss,
    page: (AppSheetPage) -> Unit,
    onTakeOutOfFolder: (AppKey) -> Unit,
    onSetMuted: (AppKey, Boolean) -> Unit,
): List<MenuAction> {
    if (!state.isFavorite) return emptyList()
    val key = state.app.key
    val folder = structure.folder
    return buildList {
        if (folder == null) {
            add(
                MenuAction(
                    R.drawable.sym_folder,
                    R.string.sheet_add_to_folder,
                    group = ROW_HOME,
                    shortLabel = R.string.sheet_short_folder,
                ) { page(AppSheetPage.Folder) },
            )
        } else {
            add(
                MenuAction(
                    R.drawable.sym_folder,
                    R.string.sheet_take_out_of_folder,
                    labelArg = folder.name,
                    group = ROW_HOME,
                    shortLabel = R.string.sheet_short_take_out,
                ) { onTakeOutOfFolder(key) }.thenDismiss(dismiss),
            )
        }
        add(
            MenuAction(
                R.drawable.sym_swipe,
                R.string.sheet_swipe_left,
                group = ROW_HOME,
                shortLabel = R.string.sheet_short_swipe_left,
            ) { page(AppSheetPage.SwipeLeft) },
        )
        if (structure.limitsOffered && state.app.isMainProfile) {
            add(
                MenuAction(
                    R.drawable.sym_hourglass_bottom,
                    R.string.sheet_daily_limit,
                    group = ROW_HOME,
                    shortLabel = R.string.sheet_short_daily_limit,
                ) { page(AppSheetPage.DailyLimit) },
            )
        }
        add(
            MenuAction(
                R.drawable.sym_do_not_disturb_on,
                R.string.sheet_mute_on_home,
                selected = structure.muted,
                group = ROW_SWITCHES,
                shortLabel = R.string.sheet_short_mute,
            ) { onSetMuted(key, !structure.muted) },
        )
    }
}
