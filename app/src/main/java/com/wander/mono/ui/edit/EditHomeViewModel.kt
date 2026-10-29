package com.wander.mono.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.calendar.CalendarAgenda
import com.wander.mono.core.calendar.CalendarEvents
import com.wander.mono.core.media.MediaSessions
import com.wander.mono.core.media.MediaState
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.NotificationPreviews
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.core.widgets.WidgetSize
import com.wander.mono.core.widgets.Widgets
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.LiveRowPrefs
import com.wander.mono.data.LiveRowRef
import com.wander.mono.data.WidgetPrefs
import com.wander.mono.data.settings.PreviewSettings
import com.wander.mono.data.settings.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Edit mode's changes to the home list: order, removal, additions, per-app live rows and widgets. */
@HiltViewModel
class EditHomeViewModel @Inject constructor(
    catalog: AppCatalog,
    media: MediaSessions,
    previews: NotificationPreviews,
    settings: SettingsStore,
    calendar: CalendarEvents,
    private val prefs: LauncherPrefs,
    private val liveRows: LiveRowPrefs,
    private val widgets: Widgets,
    private val widgetPrefs: WidgetPrefs,
) : ViewModel() {
    /** The live row switches under each installed favourite. */
    val toggles: StateFlow<Map<AppKey, List<LiveRowToggle>>> = combine(
        prefs.favorites,
        catalog.apps.filterNotNull(),
        liveSources(media, previews, settings, calendar),
        liveRows.hidden,
    ) { favorites, apps, sources, hidden ->
        val installed = apps.associateBy { it.key }
        val shown = favorites.mapNotNull(installed::get)
        val available = mergeAvailability(
            nowPlayingAvailability(sources.media, shown),
            previewAvailability(sources.previews, shown),
            calendarAvailability(sources.agenda, shown),
        )
        liveRowToggles(shown, available, hidden)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyMap())

    /** Saves a drag: the favourites' new order, then each widget under the favourite now above it. */
    fun arrange(arrangement: HomeArrangement) = launch {
        prefs.reorderFavorites(arrangement.favorites)
        widgetPrefs.edit { it.arranged(arrangement.widgets) }
    }

    fun move(key: AppKey, by: Int) = launch { prefs.moveFavorite(key, by) }

    fun remove(key: AppKey) = launch { prefs.removeFavorite(key) }

    fun add(key: AppKey) = launch { prefs.addFavorite(key) }

    fun setLiveRow(row: LiveRowRef, shown: Boolean) = launch { liveRows.setShown(row, shown) }

    fun resizeWidget(appWidgetId: Int, size: WidgetSize) = launch { widgets.resize(appWidgetId, size) }

    fun removeWidget(appWidgetId: Int) = launch { widgets.remove(appWidgetId) }

    private fun launch(change: suspend () -> Unit) {
        viewModelScope.launch { change() }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/** What live rows exist right now, before the user's switches. */
private class LiveSources(
    val media: MediaState,
    val previews: Map<PackageUser, NotificationPreview>,
    val agenda: CalendarAgenda?,
)

private fun liveSources(
    media: MediaSessions,
    previews: NotificationPreviews,
    settings: SettingsStore,
    calendar: CalendarEvents,
): Flow<LiveSources> = combine(
    media.state,
    combine(previews.previews, settings[PreviewSettings.ShowPreviews]) { all, on -> if (on) all else emptyMap() },
    calendar.agenda,
    ::LiveSources,
)
