package com.wander.mono.core.media

import androidx.compose.ui.graphics.ImageBitmap

/** The session the system ranks first, as the now-playing row shows it. */
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
    /** Null when the session reports no usable position or duration. */
    val progress: PlaybackProgress?,
)

sealed interface MediaState {
    /** Notification access is not granted, so sessions cannot be read. */
    data object NoAccess : MediaState

    data object Idle : MediaState

    data class Active(val media: PlayingMedia) : MediaState
}
