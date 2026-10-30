package com.wander.mono.core.media

import android.media.session.PlaybackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest

/**
 * When a session in [state] stops earning its row (spec 7.6): [PAUSED_LINGER_MS] after it stopped
 * playing, which is when it last reported its position ([updatedAtMs], `elapsedRealtime`), or
 * [nowMs] when it never did. Null while it plays or is on its way to (buffering, skipping).
 */
internal fun collapseAt(state: Int, updatedAtMs: Long, nowMs: Long): Long? = when (state) {
    PlaybackState.STATE_PAUSED,
    PlaybackState.STATE_STOPPED,
    PlaybackState.STATE_NONE,
    PlaybackState.STATE_ERROR,
    -> (if (updatedAtMs > 0L) updatedAtMs else nowMs) + PAUSED_LINGER_MS
    else -> null
}

/**
 * [this], turning to [MediaState.Idle] once a paused session reaches its [PlayingMedia.collapseAtMs]:
 * one wait per published state, cancelled by the next (a resume brings the row straight back).
 */
internal fun Flow<MediaState>.collapsedWhenPaused(now: () -> Long): Flow<MediaState> = transformLatest { state ->
    val at = (state as? MediaState.Active)?.media?.collapseAtMs
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

/** How long a paused session keeps its row. */
internal const val PAUSED_LINGER_MS = 10 * 60 * 1_000L
