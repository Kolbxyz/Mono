package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.ShortcutRepository
import com.wander.mono.core.usage.AppUsage
import com.wander.mono.data.FavoriteSwipes
import com.wander.mono.data.SwipeAction
import com.wander.mono.data.UsageLimits
import com.wander.mono.data.settings.ScreenTimeSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

/** Per-favourite choices home applies to its rows: the swipe left's action and the daily limit's dimming. */
@HiltViewModel
class FavoriteExtrasViewModel @Inject constructor(
    favoriteSwipes: FavoriteSwipes,
    limits: UsageLimits,
    settings: SettingsStore,
    private val usage: AppUsage,
    private val shortcuts: ShortcutRepository,
    private val actions: AppActions,
    private val messages: UserMessages,
) : ViewModel() {
    val swipes: StateFlow<Map<AppKey, SwipeAction>> =
        favoriteSwipes.actions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyMap())

    // Replayed, so home's first start is read; a start while a read runs replaces the one waiting.
    private val started = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /**
     * The favourites past their daily limit today, while the nudge is on. Usage has no event, so it
     * is read each time home comes to the screen ([recheckUsage]) and when a limit changes.
     */
    val overLimit: StateFlow<Set<AppKey>> = combine(
        settings[ScreenTimeSettings.Nudge],
        limits.limits,
        started,
    ) { on, all, _ -> if (on) all else emptyMap() }
        .mapLatest { all -> if (all.isEmpty()) emptySet() else overLimit(all, usage.todayMillisByPackage()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptySet())

    fun recheckUsage() {
        started.tryEmit(Unit)
    }

    /** Runs [app]'s swipe; a shortcut the app no longer publishes says so rather than doing nothing. */
    fun runSwipe(app: AppKey, action: SwipeAction) {
        val result = when (action) {
            SwipeAction.AppInfo -> actions.openAppInfo(app)
            is SwipeAction.Shortcut -> shortcuts.all.value
                .firstOrNull { it.id == action.id && it.belongsTo(app) }
                ?.let(actions::launchShortcut)
                ?: ActionResult.Failed(R.string.swipe_shortcut_gone)
        }
        messages.report(result)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/**
 * The apps of [limits] whose time today ([todayByPackage], Mono's own profile only) has reached
 * their limit in minutes. Without usage access (null) none is.
 */
internal fun overLimit(limits: Map<AppKey, Int>, todayByPackage: Map<String, Long>?): Set<AppKey> {
    if (todayByPackage == null) return emptySet()
    return limits.filterTo(mutableMapOf()) { (app, minutes) ->
        (todayByPackage[app.packageName] ?: 0L) >= minutes * MILLIS_PER_MINUTE
    }.keys
}

private fun AppShortcut.belongsTo(app: AppKey) = packageName == app.packageName && userSerial == app.userSerial

private const val MILLIS_PER_MINUTE = 60_000L
