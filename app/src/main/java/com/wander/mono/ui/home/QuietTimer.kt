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
 * itself is carried out by [com.wander.mono.core.quiet.QuietEnd], even while home is not shown.
 */
@Singleton
class QuietTimer @Inject constructor(
    private val quiet: QuietMode,
    private val settings: SettingsStore,
) {
    /** [QuietMode.set] also forgets the end. */
    suspend fun endNow() = quiet.set(false)

    /** Ends an hour from now, or an hour after the end already set when that is later. */
    suspend fun extendByHour(now: Long) {
        val until = settings[QuietSettings.Until].first()
        settings.set(QuietSettings.Until, quietEndAfterExtend(until, now))
    }
}

/** One hour past the later of [now] and the current end [until] (0 when none). */
internal fun quietEndAfterExtend(until: Long, now: Long): Long = maxOf(until, now) + HOUR_MS

private val HOUR_MS = TimeUnit.HOURS.toMillis(1)
