package com.wander.mono.ui.sheet

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.Routine
import com.wander.mono.data.SwipeAction
import com.wander.mono.data.UsageLimits
import com.wander.mono.ui.components.ActionButtonGroup
import com.wander.mono.ui.components.ActionEmphasis
import com.wander.mono.ui.components.MenuAction
import com.wander.mono.ui.components.SheetDismiss

/** What [AppSheetChoices] does with a choice; each slides the sheet away after. */
internal class StructureChoices(
    val onAddToFolder: (folderId: String, AppKey) -> Unit,
    val onNewFolder: (AppKey) -> Unit,
    val onSetSwipe: (AppKey, SwipeAction?) -> Unit,
    val onSetLimit: (AppKey, Int?) -> Unit,
)

/**
 * One of the app sheet's choice pages, in place of its actions: a back button and title, then the
 * choices as the same button group, the current one filled. Choosing slides the sheet away; a new
 * folder is named once it has, in a dialog.
 */
@Composable
internal fun AppSheetChoices(
    page: AppSheetPage,
    state: AppSheetState,
    structure: AppSheetStructure,
    dismiss: SheetDismiss,
    choices: StructureChoices,
    onBack: () -> Unit,
) {
    val key = state.app.key
    val actions = when (page) {
        AppSheetPage.Folder -> buildList<MenuAction> {
            add(MenuAction(R.drawable.sym_add, R.string.sheet_new_folder, ActionEmphasis.Primary) {
                choices.onNewFolder(key)
            }.afterDismiss(dismiss))
            structure.folders.forEach { folder ->
                add(MenuAction(R.drawable.sym_folder, R.string.sheet_choice, labelArg = folder.name) {
                    choices.onAddToFolder(folder.id, key)
                }.thenDismiss(dismiss))
            }
        }
        AppSheetPage.SwipeLeft -> buildList<MenuAction> {
            fun choice(@DrawableRes icon: Int, @StringRes label: Int, action: SwipeAction?, arg: String? = null) = add(
                MenuAction(icon, label, selected = structure.swipe == action, labelArg = arg) {
                    choices.onSetSwipe(key, action)
                }.thenDismiss(dismiss),
            )
            choice(R.drawable.sym_remove_circle, R.string.swipe_nothing, null)
            choice(R.drawable.sym_info, R.string.sheet_app_info, SwipeAction.AppInfo)
            state.shortcuts.forEach {
                choice(R.drawable.sym_arrow_outward, R.string.sheet_choice, SwipeAction.Shortcut(it.id), it.label)
            }
        }
        AppSheetPage.DailyLimit -> buildList<MenuAction> {
            add(MenuAction(R.drawable.sym_remove_circle, R.string.limit_none, selected = structure.dailyLimit == null) {
                choices.onSetLimit(key, null)
            }.thenDismiss(dismiss))
            UsageLimits.CHOICES.forEach { minutes ->
                add(
                    MenuAction(
                        R.drawable.sym_hourglass_bottom,
                        R.string.sheet_choice,
                        selected = structure.dailyLimit == minutes,
                        labelArg = limitLabel(minutes),
                    ) { choices.onSetLimit(key, minutes) }.thenDismiss(dismiss),
                )
            }
        }
    }
    Column {
        PageTitle(pageTitle(page), onBack)
        ActionButtonGroup(actions, Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun PageTitle(@StringRes title: Int, onBack: () -> Unit) {
    Row(
        Modifier.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(painterResource(R.drawable.sym_arrow_back), contentDescription = stringResource(R.string.action_back))
        }
        Text(
            stringResource(title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@StringRes
private fun pageTitle(page: AppSheetPage): Int = when (page) {
    AppSheetPage.Folder -> R.string.sheet_page_folder
    AppSheetPage.SwipeLeft -> R.string.sheet_page_swipe_left
    AppSheetPage.DailyLimit -> R.string.sheet_page_daily_limit
}

/** A limit as the choices name it: 30 min, 2 h, 1 h 30 min. */
@Composable
private fun limitLabel(minutes: Int): String {
    val hours = minutes / Routine.MINUTES_PER_HOUR
    val rest = minutes % Routine.MINUTES_PER_HOUR
    return when {
        hours == 0 -> stringResource(R.string.limit_minutes, rest)
        rest == 0 -> stringResource(R.string.limit_hours, hours)
        else -> stringResource(R.string.limit_hours_minutes, hours, rest)
    }
}
