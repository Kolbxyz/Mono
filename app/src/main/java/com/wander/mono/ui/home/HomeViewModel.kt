package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.calendar.CalendarActions
import com.wander.mono.core.calendar.CalendarAgenda
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.core.calendar.CalendarEvents
import com.wander.mono.core.media.ListeningDevices
import com.wander.mono.core.media.MediaSessions
import com.wander.mono.core.notifications.ListenerAccess
import com.wander.mono.core.notifications.NotificationCounts
import com.wander.mono.core.notifications.NotificationPreviews
import com.wander.mono.core.quiet.QuietMode
import com.wander.mono.core.siblings.WandaUpNext
import com.wander.mono.core.system.Battery
import com.wander.mono.core.system.SystemTime
import com.wander.mono.core.widgets.Widgets
import com.wander.mono.data.HeadphoneApps
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.LiveRowPrefs
import com.wander.mono.data.settings.BadgeSettings
import com.wander.mono.data.settings.ClockSettings
import com.wander.mono.data.settings.HeadphoneSettings
import com.wander.mono.data.settings.PreviewSettings
import com.wander.mono.data.settings.QuietSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.data.settings.clockLook
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    catalog: AppCatalog,
    prefs: LauncherPrefs,
    counts: NotificationCounts,
    previews: NotificationPreviews,
    settings: SettingsStore,
    time: SystemTime,
    battery: Battery,
    quietTimer: QuietTimer,
    liveRows: LiveRowPrefs,
    quiet: QuietMode,
    calendar: CalendarEvents,
    private val calendarActions: CalendarActions,
    widgets: Widgets,
    listening: ListeningDevices,
    headphoneApps: HeadphoneApps,
    private val media: MediaSessions,
    wandaUpNext: WandaUpNext,
    private val listenerAccess: ListenerAccess,
    private val actions: AppActions,
    private val messages: UserMessages,
) : ViewModel() {
    /** Null until the catalog is loaded, so an empty home is never shown by mistake. */
    val rows: StateFlow<List<HomeRow>?> = combine(
        prefs.favorites,
        catalog.apps.filterNotNull(),
        counts.counts,
        combine(wandaUpNext.fill(media.state), liveRows.hidden) { state, hidden -> state.unlessHidden(hidden) },
        settings[BadgeSettings.ShowCounts],
        ::assembleHomeRows,
    ).combine(
        combine(previews.previews, settings[PreviewSettings.ShowPreviews], liveRows.hidden) { all, on, hidden ->
            if (on) all.unlessHidden(hidden) else emptyMap()
        },
        ::withPreviews,
    ).combine(combine(calendar.agenda, liveRows.hidden, CalendarAgenda?::unlessHidden), ::withCalendarRow)
        .combine(widgets.placed, ::withWidgetRows)
        .combine(quiet.state, ::quietHomeRows)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** The favourites to show first: the ones chosen for headphones, while some are connected and the setting is on. */
    val lifted: StateFlow<Set<AppKey>> = combine(
        settings[HeadphoneSettings.LiftApps],
        listening.connected,
        headphoneApps.apps,
    ) { on, connected, apps -> if (on && connected) apps else emptySet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptySet())

    /** The clock lines; also where a timed quiet mode ends, on the tick this already receives. */
    val glance: StateFlow<Glance> = combine(
        combine(time.now, time.nextAlarm, calendar.agenda, rows) { now, alarm, agenda, home ->
            Glance(now, alarmWithinDay(alarm, now), glanceEvent(agenda, now, home))
        },
        // The battery receiver is registered only while its chip may show.
        settings[ClockSettings.ShowBattery].flatMapLatest { on -> if (on) battery.state else flowOf(null) },
        combine(quiet.enabled, settings[QuietSettings.Until], ::Pair),
        settings.clockLook(),
    ) { base, power, (quietOn, until), look ->
        quietTimer.settle(base.now, quietOn, until)
        base.copy(
            battery = glanceBattery(power),
            quiet = if (quietOn) QuietGlance(until) else null,
            look = look,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        Glance(System.currentTimeMillis(), null),
    )

    fun launch(app: LaunchableApp) {
        messages.report(actions.launch(app.key))
    }

    fun openEvent(event: CalendarEvent) {
        messages.report(calendarActions.open(event))
    }

    fun togglePlayPause() = media.togglePlayPause()

    fun skipNext() = media.skipNext()

    fun skipPrevious() = media.skipPrevious()

    fun seekTo(positionMs: Long) = media.seekTo(positionMs)

    fun playQueueItem(id: Long) = media.skipToQueueItem(id)

    fun openOutputSwitcher() = media.openOutputSwitcher()

    fun openListenerSettings() {
        messages.report(listenerAccess.openSettings())
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
