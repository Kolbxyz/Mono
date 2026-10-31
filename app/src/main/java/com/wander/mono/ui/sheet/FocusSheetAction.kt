package com.wander.mono.ui.sheet

import com.wander.mono.R
import com.wander.mono.core.apps.AppKey
import com.wander.mono.ui.components.MenuAction
import com.wander.mono.ui.components.SheetDismiss

/**
 * A favourite's focus, in the home row of its sheet: "Focus for 1 hour" sets every other row on
 * home back, or, while it is [focused], "End focus". Either slides the sheet away to show home
 * changing behind it. Nothing for an app not on home.
 */
internal fun focusAction(
    state: AppSheetState,
    focused: Boolean,
    dismiss: SheetDismiss,
    onFocus: (AppKey) -> Unit,
    onEndFocus: () -> Unit,
): MenuAction? = when {
    !state.isFavorite -> null
    focused -> MenuAction(R.drawable.sym_timer, R.string.focus_end, group = ROW_HOME) { onEndFocus() }
        .thenDismiss(dismiss)
    else -> MenuAction(
        R.drawable.sym_timer,
        R.string.sheet_focus_hour,
        group = ROW_HOME,
        shortLabel = R.string.sheet_short_focus,
    ) { onFocus(state.app.key) }.thenDismiss(dismiss)
}
