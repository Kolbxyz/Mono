package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppCategory
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.PrivateSpaceState
import com.wander.mono.data.settings.DrawerSort
import java.text.Collator

/**
 * The list as [choices] ask, from the profile split [drawerLists] made: Work puts the work apps in
 * the lettered list (paused profiles keep their cards, to resume them) and drops the private
 * space; Personal drops the work cards. A category keeps only its apps, everywhere. Apps new at
 * [now] and not [opened] are marked fresh while new apps are on.
 */
internal fun DrawerUiState.shaped(choices: ListChoices, opened: Set<AppKey>, now: Long): DrawerUiState {
    val hasWork = work.isNotEmpty()
    val profile = if (hasWork) choices.profile else ProfileFilter.All
    val base = when (profile) {
        ProfileFilter.Work -> work.flatMap { it.apps }.sortedWith(byLabel())
        ProfileFilter.All, ProfileFilter.Personal -> apps
    }
    val cards = when (profile) {
        ProfileFilter.All -> work
        ProfileFilter.Personal -> emptyList()
        ProfileFilter.Work -> work.map { it.copy(apps = emptyList()) }
    }
    val categories = AppCategory.entries.filter { category -> base.any { it.app.category == category } }
    val category = choices.category.takeIf { choices.showCategories && it in categories }
    val keep: (DrawerEntry) -> Boolean = { category == null || it.app.category == category }
    val mark: (DrawerEntry) -> DrawerEntry = { entry ->
        if (choices.newApps && isNew(entry.app, now, opened)) entry.copy(fresh = true) else entry
    }
    val listed = base.filter(keep).map(mark)
    val shape = when (choices.sort) {
        DrawerSort.Alphabetical -> alphabetical(listed, choices.newApps)
        DrawerSort.Recent -> byInstallAge(listed, now)
    }
    return copy(
        apps = listed,
        sections = shape.sections,
        groups = shape.groups,
        railLead = shape.lead,
        work = cards.map { card -> card.copy(apps = card.apps.filter(keep)) },
        privateApps = if (profile == ProfileFilter.Work) emptyList() else privateApps.filter(keep),
        controls = ListControls(choices.copy(profile = profile, category = category), hasWork, categories),
    )
}

/**
 * Private apps only while the space is open: once it reads locked (or gone), none are listed or
 * found, even if the catalog has not caught up with the lock yet.
 */
internal fun DrawerUiState.withPrivateSpace(space: PrivateSpaceState): DrawerUiState {
    val open = space is PrivateSpaceState.Present && !space.locked
    return if (open) {
        copy(privateSpace = space)
    } else {
        copy(privateSpace = space, privateApps = emptyList(), results = results.filterNot(::isPrivateHit))
    }
}

private fun isPrivateHit(hit: SearchHit): Boolean = when (hit) {
    is SearchHit.App -> hit.entry.app.isPrivate
    is SearchHit.Shortcut -> hit.owner?.isPrivate == true
    else -> false
}

/** A fresh collator per call: collators are not safe to share across threads. */
private fun byLabel(): Comparator<DrawerEntry> = compareBy(Collator.getInstance()) { it.app.label }
