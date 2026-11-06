package com.wander.mono.ui.drawer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.wander.mono.R
import com.wander.mono.data.settings.DrawerSort

/**
 * The drawer list's options, out of the way until asked for (a long press on the round search
 * button), so the list itself stays bare as Niagara's: whose apps (all, personal, work; only with a
 * work profile), the category chips, and newest-first order. Each choice applies at once and closes it.
 */
@Composable
internal fun ListMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    controls: ListControls,
    actions: DrawerListActions,
) {
    val title = stringResource(R.string.drawer_list_options)
    val choices = controls.choices
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.semantics { paneTitle = title },
    ) {
        if (controls.hasWork) {
            ProfileFilter.entries.forEach { filter ->
                val chosen = choices.profile == filter
                MenuChoice(profileSymbol(filter), profileLabel(filter), checked = chosen, radio = true) {
                    actions.onProfile(filter)
                    onDismiss()
                }
            }
            HorizontalDivider()
        }
        MenuChoice(R.drawable.sym_interests, R.string.drawer_show_categories, checked = choices.showCategories) {
            actions.onToggleCategories()
            onDismiss()
        }
        val recent = choices.sort == DrawerSort.Recent
        MenuChoice(R.drawable.sym_schedule, R.string.drawer_sort_by_recent, checked = recent) {
            actions.onSort(if (recent) DrawerSort.Alphabetical else DrawerSort.Recent)
            onDismiss()
        }
    }
}

/** One option: its glyph, its words, and a check while it is on; a radio choice among its peers when [radio]. */
@Composable
private fun MenuChoice(
    @DrawableRes symbol: Int,
    @StringRes label: Int,
    checked: Boolean,
    radio: Boolean = false,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(label)) },
        onClick = onClick,
        leadingIcon = { Icon(painterResource(symbol), contentDescription = null) },
        trailingIcon = if (checked) {
            { Icon(painterResource(R.drawable.sym_check), contentDescription = null) }
        } else {
            null
        },
        modifier = Modifier.semantics {
            selected = checked
            role = if (radio) Role.RadioButton else Role.Checkbox
        },
    )
}

@DrawableRes
private fun profileSymbol(filter: ProfileFilter): Int = when (filter) {
    ProfileFilter.All -> R.drawable.sym_apps
    ProfileFilter.Personal -> R.drawable.sym_person
    ProfileFilter.Work -> R.drawable.sym_work
}

@StringRes
private fun profileLabel(filter: ProfileFilter): Int = when (filter) {
    ProfileFilter.All -> R.string.drawer_profile_all
    ProfileFilter.Personal -> R.string.drawer_profile_personal
    ProfileFilter.Work -> R.string.drawer_profile_work
}
