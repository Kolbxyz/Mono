package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppCategory
import com.wander.mono.data.settings.DrawerSort

/** Whose apps the list shows: everyone's, the personal profile's (and private space), or work's. */
enum class ProfileFilter { All, Personal, Work }

/**
 * How the user set the list up: [sort] and [newApps] are settings, the rest last one opening of
 * the drawer. [category] narrows the list only while the chips show ([showCategories]).
 */
data class ListChoices(
    val sort: DrawerSort = DrawerSort.Alphabetical,
    val profile: ProfileFilter = ProfileFilter.All,
    val showCategories: Boolean = false,
    val category: AppCategory? = null,
    val newApps: Boolean = true,
)

/**
 * The toolbar's and chips' state: the [choices] in effect (All without a work profile, no category
 * once none of its apps is left), whether a work profile exists, and the categories with apps.
 */
data class ListControls(
    val choices: ListChoices = ListChoices(),
    val hasWork: Boolean = false,
    val categories: List<AppCategory> = emptyList(),
)

/** What the drawer's toolbar and chips change. */
class DrawerListActions(
    val onProfile: (ProfileFilter) -> Unit,
    val onToggleCategories: () -> Unit,
    val onCategory: (AppCategory?) -> Unit,
    val onSort: (DrawerSort) -> Unit,
)
