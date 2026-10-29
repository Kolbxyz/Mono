package com.wander.mono.ui.home

import com.wander.mono.core.quiet.QuietMode
import com.wander.mono.data.settings.QuietSettings
import com.wander.mono.data.settings.SettingsStore
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * The Quiet chip's actions: end quiet mode now, or let it run one more hour and then end. The end
 * is checked on the clock tick the header already gets, so no timer of its own runs.
 */
@Singleton
class QuietTimer @Inject constructor(
    private val quiet: QuietMode,
    private val settings: SettingsStore,
) {
    suspend fun endNow() {
        quiet.set(false)
        settings.set(QuietSettings.Until, NO_END)
    }

    /** Ends an hour from now, or an hour after the end already set when that is later. */
    suspend fun extendByHour(now: Long) {
        val until = settings[QuietSettings.Until].first()
        settings.set(QuietSettings.Until, quietEndAfterExtend(until, now))
    }

    /**
     * On each tick: quiet mode past its end is switched off, and an end left over after quiet mode
     * was switched off some other way is forgotten, so it cannot cut a later quiet session short.
     */
    suspend fun settle(now: Long, enabled: Boolean, until: Long) {
        when {
            until == NO_END -> Unit
            !enabled -> settings.set(QuietSettings.Until, NO_END)
            until <= now -> endNow()
        }
    }
}

/** One hour past the later of [now] and the current end [until] (0 when none). */
internal fun quietEndAfterExtend(until: Long, now: Long): Long = maxOf(until, now) + HOUR_MS

private const val NO_END = 0L
private val HOUR_MS = TimeUnit.HOURS.toMillis(1)
