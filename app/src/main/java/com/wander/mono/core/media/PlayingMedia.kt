package com.wander.mono.core.media

import androidx.compose.ui.graphics.ImageBitmap

/** The session the system ranks first, as the player under the clock shows it. */
data class PlayingMedia(
    val packageName: String,
    val title: String,
    val artist: String?,
    val art: ImageBitmap?,
    /** Average colour of [art] as ARGB, which tints the card. */
    val artColor: Int?,
    val isPlaying: Boolean,
    val canSkipNext: Boolean,
    val canSkipPrevious: Boolean,
    /** The session accepts `seekTo`, so the expanded bar can be dragged. */
    val canSeek: Boolean,
    /** Null when the session reports no usable position or duration. */
    val progress: PlaybackProgress?,
    /** The next few queue items; empty when the session publishes no queue or cannot jump within it. */
    val upNext: List<QueueEntry>,
    /** Where it is heard; null when the system output switcher cannot be opened from here. */
    val output: AudioOutput?,
    /**
     * When the session stopped playing, on the `SystemClock.elapsedRealtime` clock; null while it
     * plays (see [pausedAt]). The row gives way some time after, as the user chose.
     */
    val pausedAtMs: Long?,
)

sealed interface MediaState {
    /** Notification access is not granted, so sessions cannot be read. */
    data object NoAccess : MediaState

    data object Idle : MediaState

    data class Active(val media: PlayingMedia) : MediaState
}
