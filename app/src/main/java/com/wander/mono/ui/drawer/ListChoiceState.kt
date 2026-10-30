package com.wander.mono.ui.drawer

import androidx.lifecycle.SavedStateHandle
import com.wander.mono.core.apps.AppCategory
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.data.OpenedNewApps
import com.wander.mono.data.settings.DrawerSettings
import com.wander.mono.data.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

/** [choices] with the opened new apps and the moment newness was last judged, for [shaped]. */
internal data class ListInputs(val choices: ListChoices, val opened: Set<AppKey>, val now: Long)

/**
 * The drawer list's choices for the view model: the session ones (profile, category) in saved
 * state, so they survive rotation and are [clear]ed as the drawer closes; sort and new apps in
 * settings. Also notes a new app being opened, so it stops being new.
 */
internal class ListChoiceState(
    private val savedState: SavedStateHandle,
    private val settings: SettingsStore,
    private val openedNew: OpenedNewApps,
    private val scope: CoroutineScope,
) {
    private val choices: Flow<ListChoices> = combine(
        savedState.getStateFlow(PROFILE, ProfileFilter.All.name),
        savedState.getStateFlow(SHOW_CATEGORIES, false),
        savedState.getStateFlow(CATEGORY, ""),
        settings[DrawerSettings.Sort],
        settings[DrawerSettings.NewApps],
    ) { profile, showCategories, category, sort, newApps ->
        ListChoices(
            sort = sort,
            profile = ProfileFilter.entries.firstOrNull { it.name == profile } ?: ProfileFilter.All,
            showCategories = showCategories,
            category = AppCategory.entries.firstOrNull { it.name == category },
            newApps = newApps,
        )
    }

    fun inputs(apps: Flow<List<LaunchableApp>>): Flow<ListInputs> =
        combine(choices, openedNew.opened, newnessClock(apps), ::ListInputs)

    val actions = DrawerListActions(
        onProfile = { savedState[PROFILE] = it.name },
        onToggleCategories = {
            savedState[SHOW_CATEGORIES] = savedState.get<Boolean>(SHOW_CATEGORIES) != true
            savedState[CATEGORY] = ""
        },
        onCategory = { savedState[CATEGORY] = it?.name.orEmpty() },
        onSort = { sort -> scope.launch { settings.set(DrawerSettings.Sort, sort) } },
    )

    fun clear() {
        savedState[PROFILE] = ProfileFilter.All.name
        savedState[SHOW_CATEGORIES] = false
        savedState[CATEGORY] = ""
    }

    /** After [app] opened: if it was new, it no longer is. [all] is the catalog, to keep only keys still new. */
    fun opened(app: LaunchableApp, all: List<LaunchableApp>) {
        val now = System.currentTimeMillis()
        if (!isNew(app, now, emptySet())) return
        val stillNew = all.filter { isNew(it, now, emptySet()) }.map { it.key }.toSet()
        scope.launch { openedNew.markOpened(app.key, stillNew) }
    }

    /**
     * The time newness is judged at: now on each catalog change, then again when the next new app
     * stops being new. One timer to the next boundary, not polling.
     */
    private fun newnessClock(apps: Flow<List<LaunchableApp>>): Flow<Long> = apps.transformLatest { list ->
        val installTimes = list.mapNotNull { it.installedAt }
        while (true) {
            val now = System.currentTimeMillis()
            emit(now)
            val next = nextNewExpiry(installTimes, now) ?: break
            delay(next - now)
        }
    }

    private companion object {
        const val PROFILE = "list_profile"
        const val SHOW_CATEGORIES = "list_show_categories"
        const val CATEGORY = "list_category"
    }
}
