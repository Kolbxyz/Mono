package com.wander.mono.ui.sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.FavoriteSwipes
import com.wander.mono.data.HomeFolder
import com.wander.mono.data.HomeFolders
import com.wander.mono.data.MutedApps
import com.wander.mono.data.SwipeAction
import com.wander.mono.data.UsageLimits
import com.wander.mono.data.settings.ScreenTimeSettings
import com.wander.mono.data.settings.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * How an app's options shape home: its folder, its swipe left, its notifications on home and its
 * daily limit. Kept apart from [AppSheetViewModel], which owns what the sheet is about.
 */
@HiltViewModel
class AppSheetStructureViewModel @Inject constructor(
    folders: HomeFolders,
    private val swipes: FavoriteSwipes,
    private val muted: MutedApps,
    private val limits: UsageLimits,
    settings: SettingsStore,
) : ViewModel() {
    // Eager: the sheet opens with its rows already there, not a frame later.
    private val all: StateFlow<Everything?> = combine(
        folders.folders,
        swipes.actions,
        muted.apps,
        limits.limits,
        settings[ScreenTimeSettings.Nudge],
        ::Everything,
    ).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun of(app: AppKey): Flow<AppSheetStructure> = all.filterNotNull().map { it.of(app) }

    fun setSwipe(app: AppKey, action: SwipeAction?) {
        viewModelScope.launch { swipes.set(app, action) }
    }

    /** Keeps the sheet open, so the switch is seen to change. */
    fun setMuted(app: AppKey, muted: Boolean) {
        viewModelScope.launch { this@AppSheetStructureViewModel.muted.setMuted(app, muted) }
    }

    fun setLimit(app: AppKey, minutes: Int?) {
        viewModelScope.launch { limits.set(app, minutes) }
    }

    private class Everything(
        val folders: List<HomeFolder>,
        val swipes: Map<AppKey, SwipeAction>,
        val muted: Set<AppKey>,
        val limits: Map<AppKey, Int>,
        val nudge: Boolean,
    ) {
        fun of(app: AppKey) = AppSheetStructure(
            folder = folders.firstOrNull { app in it.apps },
            folders = folders,
            swipe = swipes[app],
            muted = app in muted,
            dailyLimit = limits[app],
            limitsOffered = nudge,
        )
    }
}

/**
 * One app's place in home's structure. [folder] is the one holding it, among all [folders];
 * [limitsOffered] is whether the screen-time nudge is on, without which a limit does nothing.
 */
data class AppSheetStructure(
    val folder: HomeFolder?,
    val folders: List<HomeFolder>,
    val swipe: SwipeAction?,
    val muted: Boolean,
    val dailyLimit: Int?,
    val limitsOffered: Boolean,
)
