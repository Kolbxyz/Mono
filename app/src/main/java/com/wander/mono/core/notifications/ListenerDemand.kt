package com.wander.mono.core.notifications

import com.wander.mono.core.quiet.QuietMode
import com.wander.mono.di.AppScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Whether anything needs the listener's readings right now: home on screen (its badges, previews
 * and live rows), or a quiet session, whose [QuietDigest] compares every reading. Otherwise the
 * listener only notes that something changed and reads once when this turns true again, instead of
 * re-reading every notification on each post all day with the screen off.
 */
@Singleton
class ListenerDemand @Inject constructor(quiet: QuietMode, @AppScope scope: CoroutineScope) {
    private val homeShown = MutableStateFlow(false)

    val wanted: StateFlow<Boolean> = combine(homeShown, quiet.enabled) { shown, quietOn -> shown || quietOn }
        .stateIn(scope, SharingStarted.Eagerly, false)

    /** Called by the activity from `onStart` / `onStop`. */
    fun setHomeShown(shown: Boolean) {
        homeShown.value = shown
    }
}
