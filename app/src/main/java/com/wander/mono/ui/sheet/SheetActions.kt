package com.wander.mono.ui.sheet

import com.wander.mono.ui.components.MenuAction
import com.wander.mono.ui.components.SheetDismiss

/** This action, then the sheet slides away with the action's effect showing behind it. */
internal fun MenuAction.thenDismiss(dismiss: SheetDismiss): MenuAction = withClick {
    onClick()
    dismiss()
}

/**
 * The sheet slides away first, then this action: for one whose caller drops the sheet (or opens
 * another overlay) the moment it runs, which would otherwise cut the sheet off mid-screen.
 */
internal fun MenuAction.afterDismiss(dismiss: SheetDismiss): MenuAction = withClick { dismiss(onClick) }

private fun MenuAction.withClick(click: () -> Unit) = MenuAction(icon, label, emphasis, selected, labelArg, click)
