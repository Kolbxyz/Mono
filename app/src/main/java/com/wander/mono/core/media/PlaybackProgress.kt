package com.wander.mono.core.media

/**
 * Where playback stood when the session last reported it. Sessions only report on changes, so the
 * current position is extrapolated from [updatedAtMs] (on the `SystemClock.elapsedRealtime` clock)
 * at [speed] while [isAdvancing].
 */
data class PlaybackProgress(
    val positionMs: Long,
    val durationMs: Long,
    val speed: Float,
    val updatedAtMs: Long,
    val isAdvancing: Boolean,
) {
    /** The played fraction at [nowMs], clamped to 0..1. */
    fun fractionAt(nowMs: Long): Float {
        val elapsed = if (isAdvancing) ((nowMs - updatedAtMs).coerceAtLeast(0L) * speed).toLong() else 0L
        return ((positionMs + elapsed).toFloat() / durationMs).coerceIn(0f, 1f)
    }
}

/**
 * The progress to show, or null when the session does not report a usable position or duration
 * (live streams, some podcasts): no bar beats a made-up one.
 */
internal fun playbackProgress(
    positionMs: Long,
    durationMs: Long,
    speed: Float,
    updatedAtMs: Long,
    isPlaying: Boolean,
): PlaybackProgress? {
    if (positionMs < 0L || durationMs <= 0L) return null
    return PlaybackProgress(positionMs, durationMs, speed, updatedAtMs, isAdvancing = isPlaying && speed > 0f)
}
