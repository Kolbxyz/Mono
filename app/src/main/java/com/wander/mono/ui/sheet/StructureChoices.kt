package com.wander.mono.ui.sheet

import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.SwipeAction

/** What [AppSheetChoices] does with a choice; each slides the sheet away after. */
internal class StructureChoices(
    val onAddToFolder: (folderId: String, AppKey) -> Unit,
    val onNewFolder: (AppKey) -> Unit,
    val onSetSwipe: (AppKey, SwipeAction?) -> Unit,
    val onSetLimit: (AppKey, Int?) -> Unit,
)
