package com.wander.mono.ui.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.IconCache
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.usage.AppUsage
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.settings.IconMode
import com.wander.mono.data.settings.IconSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.ui.components.AppIcons
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The favourites step: what it lists, in order, and what is ticked, in the order it was ticked. */
data class FavoritePicks(val apps: List<LaunchableApp>, val picked: List<AppKey>)

/**
 * The favourites step's checklist, ordered by today's usage when Mono may read it (read with each
 * catalog load, never on a timer). The ticks survive rotation and process death, and reach home
 * only on [save], so Skip adds nothing.
 */
@HiltViewModel
class FavoritePickerViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    catalog: AppCatalog,
    usage: AppUsage,
    private val favorites: LauncherPrefs,
    private val iconCache: IconCache,
    settings: SettingsStore,
) : ViewModel() {
    private val suggestions: StateFlow<FavoriteSuggestions?> = catalog.apps
        .filterNotNull()
        .map { apps -> favoriteSuggestions(apps, usage.todayMillisByPackage()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Encoded keys; null until the suggestions first tick some, so a restored choice is kept. */
    private val ticked = savedState.getStateFlow<ArrayList<String>?>(PICKED, null)

    /** Null until the apps are loaded. */
    val picks: StateFlow<FavoritePicks?> = combine(suggestions, ticked) { suggested, encoded ->
        suggested?.let {
            val picked = encoded?.mapNotNull(AppKey::decode) ?: it.preselected
            FavoritePicks(it.apps, picked)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Icons as home and the drawer draw them, in the user's mode and pack (a restored backup's, say). */
    val icons: StateFlow<AppIcons> = combine(settings[IconSettings.Mode], settings[IconSettings.Pack]) { mode, pack ->
        AppIcons(mode, ::icon, ::cachedIcon, pack)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppIcons(IconSettings.Mode.default, ::icon, ::cachedIcon))

    private suspend fun icon(key: AppKey, mode: IconMode, sizePx: Int): AppIconImage? =
        iconCache.icon(key, themed = mode == IconMode.Themed, sizePx)

    private fun cachedIcon(key: AppKey, mode: IconMode, sizePx: Int): AppIconImage? =
        iconCache.cached(key, themed = mode == IconMode.Themed, sizePx)

    fun toggle(key: AppKey) {
        val current = picks.value?.picked ?: return
        val next = if (key in current) current - key else current + key
        savedState[PICKED] = ArrayList(next.map { it.encode() })
    }

    /** Adds the ticked apps to home, below any favourites already there, in the order they were ticked. */
    fun save() {
        val picked = picks.value?.picked ?: return
        viewModelScope.launch { picked.forEach { favorites.addFavorite(it) } }
    }

    private companion object {
        const val PICKED = "onboarding_picked_favorites"
    }
}
