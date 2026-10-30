package com.wander.mono.data.backup

import com.wander.mono.data.FavoriteSwipes
import com.wander.mono.data.HomeFolders
import com.wander.mono.data.LiveRowPrefs
import com.wander.mono.data.MutedApps
import com.wander.mono.data.Routines
import com.wander.mono.data.UsageLimits
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/** The stores behind a [HomeStructure], read and written together for a backup. */
@Singleton
class StructureStores @Inject constructor(
    private val folders: HomeFolders,
    private val swipes: FavoriteSwipes,
    private val muted: MutedApps,
    private val limits: UsageLimits,
    private val routines: Routines,
    private val liveRows: LiveRowPrefs,
) {
    suspend fun current() = HomeStructure(
        folders = folders.folders.first(),
        swipes = swipes.actions.first(),
        mutedApps = muted.apps.first(),
        dailyLimits = limits.limits.first(),
        routines = routines.routines.first(),
        nowPlaying = liveRows.nowPlayingAnchor.first(),
    )

    /** Each store in one edit of its own, as the rest of a restore. */
    suspend fun restore(structure: HomeStructure) {
        folders.replace(structure.folders)
        swipes.replace(structure.swipes)
        muted.replace(structure.mutedApps)
        limits.replace(structure.dailyLimits)
        routines.replace(structure.routines)
        liveRows.setNowPlayingAnchor(structure.nowPlaying)
    }
}
