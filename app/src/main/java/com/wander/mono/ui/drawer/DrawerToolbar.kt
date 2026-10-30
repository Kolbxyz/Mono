package com.wander.mono.ui.drawer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.data.settings.DrawerSort

/**
 * The drawer list's floating toolbar, in the thumb zone [bottom] above the screen's edge: whose apps
 * (all, personal, work; only with a work profile), the category chips, and newest-first order.
 * Slides away while not [visible], as the list scrolls on.
 */
@Composable
internal fun DrawerToolbar(
    visible: Boolean,
    controls: ListControls,
    actions: DrawerListActions,
    bottom: Dp,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(motion.defaultSpatialSpec()) { it } + fadeIn(motion.defaultEffectsSpec()),
        exit = slideOutVertically(motion.fastSpatialSpec()) { it } + fadeOut(motion.fastEffectsSpec()),
    ) {
        val title = stringResource(R.string.drawer_toolbar)
        val choices = controls.choices
        HorizontalFloatingToolbar(
            expanded = true,
            colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
            modifier = Modifier.padding(bottom = bottom + TOOLBAR_MARGIN).semantics { paneTitle = title },
        ) {
            if (controls.hasWork) {
                Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ProfileFilter.entries.forEach { filter ->
                        ToolbarToggle(
                            checked = choices.profile == filter,
                            symbol = profileSymbol(filter),
                            label = profileLabel(filter),
                            onChecked = { actions.onProfile(filter) },
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
            }
            ToolbarToggle(
                checked = choices.showCategories,
                symbol = R.drawable.sym_interests,
                label = R.string.drawer_show_categories,
                onChecked = { actions.onToggleCategories() },
            )
            Spacer(Modifier.width(4.dp))
            ToolbarToggle(
                checked = choices.sort == DrawerSort.Recent,
                symbol = R.drawable.sym_schedule,
                label = R.string.drawer_sort_by_recent,
                onChecked = { recent -> actions.onSort(if (recent) DrawerSort.Recent else DrawerSort.Alphabetical) },
            )
        }
    }
}

/** A round icon toggle: filled in primary while on, a tonal step above the toolbar while off. */
@Composable
private fun ToolbarToggle(
    checked: Boolean,
    @DrawableRes symbol: Int,
    @StringRes label: Int,
    onChecked: (Boolean) -> Unit,
) {
    FilledIconToggleButton(checked = checked, onCheckedChange = onChecked) {
        Icon(painterResource(symbol), contentDescription = stringResource(label))
    }
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

/** The toolbar's gap above the navigation bar. */
private val TOOLBAR_MARGIN = 16.dp

/** How much of the list's bottom the toolbar covers above the navigation bar, so the last row scrolls clear. */
internal val DrawerToolbarClearance: Dp = FloatingToolbarDefaults.ContainerSize + TOOLBAR_MARGIN + 8.dp
