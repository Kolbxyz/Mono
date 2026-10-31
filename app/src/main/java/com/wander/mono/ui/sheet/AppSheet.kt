package com.wander.mono.ui.sheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.IconActionRows
import com.wander.mono.ui.components.MonoSheet
import com.wander.mono.ui.components.rememberCredentialPrompt
import com.wander.mono.ui.home.folders.FolderViewModel

/**
 * Long-press options for one app: its icon, name (a small lock after it while locked) and today's
 * use as a chip, then a wide "Open" with the app's shortcuts as chips under it, then its actions as
 * rows of icon buttons grouped by meaning ([appActions]). A structure choice (folder, swipe left,
 * daily limit) opens its page in place of the actions. [loadShortcutIcon] draws the shortcuts'
 * own icons.
 */
@Composable
fun AppSheet(
    state: AppSheetState,
    icons: AppIcons,
    usageToday: SheetUsage,
    onDismiss: () -> Unit,
    onOpen: (AppKey) -> Unit,
    onShortcut: (AppShortcut) -> Unit,
    loadShortcutIcon: SheetShortcutIcon,
    onAddToHome: (AppKey) -> Unit,
    onRemoveFromHome: (AppKey) -> Unit,
    onSetHidden: (AppKey, Boolean) -> Unit,
    onSetLiftsWithHeadphones: (AppKey, Boolean) -> Unit,
    onRename: (AppKey) -> Unit,
    onAppInfo: (AppKey) -> Unit,
    onUninstall: (AppKey) -> Unit,
    onSetLocked: (AppKey, Boolean) -> Unit,
    structure: AppSheetStructureViewModel = viewModel(),
    folders: FolderViewModel = viewModel(),
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
    val shape by remember(key) { structure.of(key) }.collectAsStateWithLifecycle(null)
    var page by rememberSaveable(key) { mutableStateOf<AppSheetPage?>(null) }
    val motion = MaterialTheme.motionScheme
    MonoSheet(onDismissRequest = onDismiss) { animatedDismiss ->
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            SheetHeader(
                title = state.app.label,
                leading = { AppIcon(key, icons, size = 56.dp) },
                supporting = if (usageToday != SheetUsage.Hidden) {
                    { UsageSlot(usageToday) }
                } else {
                    null
                },
                badge = { LockBadge(state.isLocked) },
            )
            OpenButton(
                locked = state.isLocked,
                onOpen = {
                    onOpen(key)
                    animatedDismiss()
                },
                modifier = Modifier.padding(top = 8.dp),
            )
            if (state.shortcuts.isNotEmpty()) {
                ShortcutRow(
                    app = key,
                    shortcuts = state.shortcuts,
                    icons = icons,
                    loadIcon = loadShortcutIcon,
                    onShortcut = { shortcut ->
                        onShortcut(shortcut)
                        animatedDismiss()
                    },
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    (fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()))
                        .using(SizeTransform { _, _ -> motion.defaultSpatialSpec() })
                },
                label = "app sheet page",
            ) { shown ->
                val known = shape
                if (shown != null && known != null) {
                    AppSheetChoices(
                        page = shown,
                        state = state,
                        structure = known,
                        dismiss = animatedDismiss,
                        choices = StructureChoices(
                            onAddToFolder = folders::addTo,
                            onNewFolder = folders::startNewFolder,
                            onSetSwipe = structure::setSwipe,
                            onSetLimit = structure::setLimit,
                        ),
                        onBack = { page = null },
                    )
                } else {
                    IconActionRows(
                        appActions(
                            state,
                            animatedDismiss,
                            onAddToHome,
                            onRemoveFromHome,
                            onSetHidden,
                            onSetLiftsWithHeadphones,
                            onRename,
                            onAppInfo,
                            onUninstall,
                            setLocked,
                            more = known?.let {
                                structureActions(
                                    state = state,
                                    structure = it,
                                    dismiss = animatedDismiss,
                                    page = { next -> page = next },
                                    onTakeOutOfFolder = folders::takeOut,
                                    onSetMuted = structure::setMuted,
                                )
                            }.orEmpty(),
                        ),
                        Modifier.padding(top = 20.dp),
                    )
                }
            }
        }
    }
}
