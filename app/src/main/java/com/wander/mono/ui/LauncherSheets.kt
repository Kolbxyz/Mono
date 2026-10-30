package com.wander.mono.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.quiet.QuietViewModel
import com.wander.mono.ui.sheet.AppSheet
import com.wander.mono.ui.sheet.AppSheetViewModel
import com.wander.mono.ui.sheet.HomeSheet
import com.wander.mono.ui.sheet.RenameDialog
import com.wander.mono.ui.sheet.RenameViewModel
import com.wander.mono.ui.widgets.WidgetSheets
import com.wander.mono.ui.widgets.WidgetsViewModel

/**
 * What opens over everything from a long press: home's own options, a widget's, an app's (drawing
 * its icon with [icons]) and the rename dialog.
 */
@Composable
fun LauncherSheets(
    launcher: LauncherViewModel,
    sheet: AppSheetViewModel,
    rename: RenameViewModel,
    quiet: QuietViewModel,
    widgets: WidgetsViewModel,
    icons: AppIcons,
) {
    val homeOptionsOpen by launcher.homeOptionsOpen.collectAsStateWithLifecycle()
    val quietOn by quiet.enabled.collectAsStateWithLifecycle()
    val sheetState by sheet.state.collectAsStateWithLifecycle()
    val sheetUsage by sheet.usageToday.collectAsStateWithLifecycle()
    if (homeOptionsOpen) {
        HomeSheet(
            quiet = quietOn,
            onDismiss = { launcher.showHomeOptions(false) },
            onSetQuiet = {
                launcher.showHomeOptions(false)
                quiet.setEnabled(it)
            },
            onOpenSettings = launcher::openSettings,
            onAddWidget = {
                launcher.showHomeOptions(false)
                widgets.openPicker()
            },
            onEditHome = { launcher.setEditing(true) },
        )
    }
    WidgetSheets(widgets)
    sheetState?.let { state ->
        AppSheet(
            state = state,
            icons = icons,
            usageToday = sheetUsage,
            onDismiss = sheet::dismiss,
            onOpen = sheet::open,
            onShortcut = sheet::launchShortcut,
            onAddToHome = sheet::addToHome,
            onRemoveFromHome = sheet::removeFromHome,
            onMove = sheet::move,
            onSetHidden = sheet::setHidden,
            onSetLiftsWithHeadphones = sheet::setLiftsWithHeadphones,
            onRename = {
                sheet.dismiss()
                rename.show(it)
            },
            onAppInfo = sheet::openAppInfo,
            onUninstall = sheet::uninstall,
            onSetLocked = sheet::setLocked,
        )
    }
    RenameDialog(rename)
}
