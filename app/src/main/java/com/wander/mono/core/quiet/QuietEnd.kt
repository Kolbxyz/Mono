package com.wander.mono.core.quiet

import com.wander.mono.data.settings.QuietSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.di.AppScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Ends a timed quiet mode at its end, whether or not home is on screen, so the linked Do Not
 * Disturb mode never outlives it. One wait per end that is set, restarted whenever the end or the
 * switch changes; nothing is polled. A wait cut short by the process dying is caught up on the next
 * start, since an end already past is acted on at once.
 */
@Singleton
class QuietEnd @Inject constructor(
    private val quiet: QuietMode,
    private val settings: SettingsStore,
    @AppScope private val scope: CoroutineScope,
) {
    /** Called once, when the process starts. */
    fun start() {
        scope.launch {
            combine(quiet.enabled, settings[QuietSettings.Until], ::Pair).collectLatest { (on, until) ->
                when {
                    until == QuietSettings.Until.default -> Unit
                    // Switched off some other way: forget the end so it cannot cut a later session short.
                    !on -> settings.set(QuietSettings.Until, QuietSettings.Until.default)
                    else -> {
                        delay(until - System.currentTimeMillis())
                        // Its own first write restarts this collector; finish the switch (and the
                        // Do Not Disturb mode) regardless.
                        withContext(NonCancellable) { quiet.set(false) }
                    }
                }
            }
        }
    }
}
