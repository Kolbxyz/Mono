package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.WorkProfile

/** A work profile at the bottom of the drawer; [apps] is empty while it is paused. */
data class WorkSection(val profile: WorkProfile, val apps: List<DrawerEntry>)

/** The A–Z list (main profile only), the private space's own section and one per work profile. */
internal data class DrawerLists(
    val apps: List<DrawerEntry>,
    val privateApps: List<DrawerEntry>,
    val work: List<WorkSection>,
)

/**
 * Hidden apps go in no list; private space and work apps only in their own sections, so the letter
 * rail covers the A–Z list alone. The catalog holds private apps only while the space is unlocked; a
 * paused work profile's apps are held back here. Apps of a work profile [work] does not know yet (it
 * was just added) wait until it does rather than show up in the A–Z list.
 */
internal fun drawerLists(all: List<DrawerEntry>, hidden: Set<AppKey>, work: List<WorkProfile>): DrawerLists {
    val visible = all.filter { it.app.key !in hidden }
    val (mainApps, others) = visible.partition { it.app.isMainProfile }
    val (privateApps, workApps) = others.partition { it.app.isPrivate }
    val bySerial = workApps.groupBy { it.app.key.userSerial }
    return DrawerLists(
        apps = mainApps,
        privateApps = privateApps,
        work = work.map { profile ->
            WorkSection(profile, if (profile.paused) emptyList() else bySerial[profile.serial].orEmpty())
        },
    )
}

/** What search may find: everything but a paused work profile's apps, which could not open. */
internal fun searchableApps(all: List<DrawerEntry>, work: List<WorkProfile>): List<DrawerEntry> {
    val paused = work.filter { it.paused }.map { it.serial }.toSet()
    return all.filter { it.app.key.userSerial !in paused }
}

/** Shortcuts of a paused work profile stay out of search like its apps. */
internal fun searchableShortcuts(shortcuts: List<AppShortcut>, work: List<WorkProfile>): List<AppShortcut> {
    val paused = work.filter { it.paused }.map { it.serial }.toSet()
    return shortcuts.filter { it.userSerial !in paused }
}

/** While picking an app for home, private ones are not offered: they never go on home. */
internal fun pickable(results: List<SearchHit>): List<SearchHit> =
    results.filterNot { it is SearchHit.App && it.entry.app.isPrivate }
