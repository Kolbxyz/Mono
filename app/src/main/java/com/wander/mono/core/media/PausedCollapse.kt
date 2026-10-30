package com.wander.mono.core.media

import android.media.session.PlaybackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest

/**
 * When a session in [state] stopped playing: when it last reported its position ([updatedAtMs],
 * `elapsedRealtime`), or [nowMs] when it never did. Null while it plays or is on its way to
 * (buffering, skipping).
 */
internal fun pausedAt(state: Int, updatedAtMs: Long, nowMs: Long): Long? = when (state) {
    PlaybackState.STATE_PAUSED,
    PlaybackState.STATE_STOPPED,
    PlaybackState.STATE_NONE,
    PlaybackState.STATE_ERROR,
    -> if (updatedAtMs > 0L) updatedAtMs else nowMs
    else -> null
}

/**
 * When a session paused at [pausedAtMs] stops earning its row (spec 7.6): [lingerMs] later. Null
 * while it plays, or when the user keeps paused cards ([lingerMs] null).
 */
internal fun collapseAt(pausedAtMs: Long?, lingerMs: Long?): Long? =
    if (pausedAtMs != null && lingerMs != null) pausedAtMs + lingerMs else null

/**
 * [this], turning to [MediaState.Idle] once a paused session has been paused for [lingerMs] (the
 * user's choice; null never): one wait per published state or choice, cancelled by the next (a
 * resume brings the row straight back).
 */
internal fun Flow<MediaState>.collapsedWhenPaused(lingerMs: Flow<Long?>, now: () -> Long): Flow<MediaState> =
    combine(this, lingerMs) { state, linger -> state to linger }.transformLatest { (state, linger) ->
        val at = collapseAt((state as? MediaState.Active)?.media?.pausedAtMs, linger)
        if (at == null) {
            emit(state)
            return@transformLatest
        }
        val wait = at - now()
        if (wait > 0L) {
            emit(state)
            delay(wait)
        }
        emit(MediaState.Idle)
    }
