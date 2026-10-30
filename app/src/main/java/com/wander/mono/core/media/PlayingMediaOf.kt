package com.wander.mono.core.media

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.SystemClock
import androidx.compose.ui.graphics.asImageBitmap

/** Whether the session has something to show at all: a session without a title gets no row. */
internal fun MediaController.hasTitle() = metadata?.title() != null

/** What the now-playing row shows for [controller], or null when it has no titled metadata. */
internal fun playingMedia(controller: MediaController, art: ArtMemo, output: AudioOutput?): PlayingMedia? {
    val metadata = controller.metadata
    val title = metadata?.title() ?: return null
    val bitmap = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
        ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
        ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
    val playback = controller.playbackState
    val isPlaying = playback?.state == PlaybackState.STATE_PLAYING
    val actions = playback?.actions ?: 0L
    return PlayingMedia(
        packageName = controller.packageName,
        title = title,
        artist = (metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)),
        art = bitmap?.asImageBitmap(),
        artColor = bitmap?.let(art::colorOf),
        isPlaying = isPlaying,
        canSkipNext = actions and PlaybackState.ACTION_SKIP_TO_NEXT != 0L,
        canSkipPrevious = actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS != 0L,
        canSeek = actions and PlaybackState.ACTION_SEEK_TO != 0L,
        progress = playback?.let {
            playbackProgress(
                positionMs = it.position,
                durationMs = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION),
                speed = it.playbackSpeed,
                updatedAtMs = it.lastPositionUpdateTime,
                isPlaying = isPlaying,
            )
        },
        upNext = controller.upNext(),
        output = output,
        pausedAtMs = playback?.let { pausedAt(it.state, it.lastPositionUpdateTime, SystemClock.elapsedRealtime()) },
    )
}

private fun MediaMetadata.title(): String? =
    getString(MediaMetadata.METADATA_KEY_TITLE) ?: getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
