package com.wander.mono.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.LocalSheetBack
import com.wander.mono.ui.components.SheetBack
import com.wander.mono.ui.quiet.QuietViewModel
import com.wander.mono.ui.sheet.AppSheet
import com.wander.mono.ui.sheet.AppSheetViewModel
import com.wander.mono.ui.sheet.ClockSheet
import com.wander.mono.ui.sheet.HomeSheet
import com.wander.mono.ui.sheet.MediaSheet
import com.wander.mono.ui.sheet.RenameDialog
import com.wander.mono.ui.sheet.RenameViewModel
import com.wander.mono.ui.widgets.WidgetSheets
import com.wander.mono.ui.widgets.WidgetsViewModel

/**
 * Whether any of [LauncherSheets] is open, so the layers under them recede: home's options, the
 * clock's or the media card's, an app's, the widget picker or a widget's options, and the rename
 * dialog.
 */
@Composable
fun anySheetOpen(
    launcher: LauncherViewModel,
    sheet: AppSheetViewModel,
    rename: RenameViewModel,
    widgets: WidgetsViewModel,
): Boolean {
    val homeOptionsOpen by launcher.homeOptionsOpen.collectAsStateWithLifecycle()
    val clockSheetOpen by launcher.clockSheetOpen.collectAsStateWithLifecycle()
    val mediaSheetOpen by launcher.mediaSheetOpen.collectAsStateWithLifecycle()
    val appSheet by sheet.state.collectAsStateWithLifecycle()
    val pickerOpen by widgets.pickerOpen.collectAsStateWithLifecycle()
    val widgetOptions by widgets.options.collectAsStateWithLifecycle()
    val renaming by rename.app.collectAsStateWithLifecycle()
    return homeOptionsOpen || clockSheetOpen || mediaSheetOpen || appSheet != null || pickerOpen ||
        widgetOptions != null || renaming != null
}

/**
 * What opens over everything from a long press: home's own options, the clock's and the media
 * card's sheets, a widget's, an app's (drawing its icon with [icons]) and the rename dialog.
 * Their back swipes go to [back], which the layers under them follow.
 */
@Composable
fun LauncherSheets(
    launcher: LauncherViewModel,
    sheet: AppSheetViewModel,
    rename: RenameViewModel,
    quiet: QuietViewModel,
    widgets: WidgetsViewModel,
    icons: AppIcons,
    back: SheetBack,
) {
    CompositionLocalProvider(LocalSheetBack provides back) {
        Sheets(launcher, sheet, rename, quiet, widgets, icons)
    }
}

@Composable
private fun Sheets(
    launcher: LauncherViewModel,
    sheet: AppSheetViewModel,
    rename: RenameViewModel,
    quiet: QuietViewModel,
    widgets: WidgetsViewModel,
    icons: AppIcons,
) {
    val homeOptionsOpen by launcher.homeOptionsOpen.collectAsStateWithLifecycle()
    val clockSheetOpen by launcher.clockSheetOpen.collectAsStateWithLifecycle()
    val mediaSheetOpen by launcher.mediaSheetOpen.collectAsStateWithLifecycle()
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
    if (clockSheetOpen) ClockSheet(onDismiss = { launcher.showClockSheet(false) })
    if (mediaSheetOpen) MediaSheet(onDismiss = { launcher.showMediaSheet(false) })
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
