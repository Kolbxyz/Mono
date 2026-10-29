package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey

/** The A–Z list and the private space's own section. */
internal data class DrawerLists(val apps: List<DrawerEntry>, val privateApps: List<DrawerEntry>)

/**
 * Hidden apps go in neither list; private space apps only in their own section, so the letter rail
 * covers the A–Z list alone. The catalog holds private apps only while the space is unlocked.
 */
internal fun drawerLists(all: List<DrawerEntry>, hidden: Set<AppKey>): DrawerLists {
    val (privateApps, apps) = all.filter { it.app.key !in hidden }.partition { it.app.isPrivate }
    return DrawerLists(apps, privateApps)
}

/** While picking an app for home, private ones are not offered: they never go on home. */
internal fun pickable(results: List<SearchHit>): List<SearchHit> =
    results.filterNot { it is SearchHit.App && it.entry.app.isPrivate }
