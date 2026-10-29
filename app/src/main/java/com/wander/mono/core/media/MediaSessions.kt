package com.wander.mono.core.media

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.graphics.asImageBitmap
import com.wander.mono.core.notifications.ListenerAccess
import com.wander.mono.di.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/**
 * The active media sessions, read through the notification listener's grant. Watches the session
 * list and each controller's callbacks while the home screen is visible; nothing is polled.
 */
@Singleton
class MediaSessions @Inject constructor(
    @ApplicationContext context: Context,
    private val access: ListenerAccess,
    @AppScope scope: CoroutineScope,
) {
    private val sessionManager = context.getSystemService(MediaSessionManager::class.java)

    /** The controller behind the published state, for the transport buttons. Main thread only. */
    private var current: MediaController? = null

    val state: StateFlow<MediaState> = access.granted
        .flatMapLatest { granted -> if (granted) sessions() else flowOf(MediaState.NoAccess) }
        .stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), MediaState.Idle)

    fun togglePlayPause() {
        val controller = current ?: return
        if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }
    }

    fun skipNext() {
        current?.transportControls?.skipToNext()
    }

    fun skipPrevious() {
        current?.transportControls?.skipToPrevious()
    }

    private fun sessions(): Flow<MediaState> = callbackFlow {
        val handler = Handler(Looper.getMainLooper())
        val watched = mutableMapOf<MediaController, MediaController.Callback>()
        val art = ArtMemo()

        fun publish() {
            val chosen = pick(watched.keys.toList())
            current = chosen
            trySend(chosen?.let { describe(it, art) }?.let(MediaState::Active) ?: MediaState.Idle)
        }

        fun watch(controllers: List<MediaController>) {
            val tokens = controllers.map { it.sessionToken }.toSet()
            watched.keys.filter { it.sessionToken !in tokens }.forEach { gone ->
                watched.remove(gone)?.let(gone::unregisterCallback)
            }
            val known = watched.keys.map { it.sessionToken }.toSet()
            controllers.filter { it.sessionToken !in known }.forEach { controller ->
                val callback = object : MediaController.Callback() {
                    override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
                    override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
                }
                controller.registerCallback(callback, handler)
                watched[controller] = callback
            }
            publish()
        }

        val listener = MediaSessionManager.OnActiveSessionsChangedListener { watch(it.orEmpty()) }
        try {
            sessionManager.addOnActiveSessionsChangedListener(listener, access.component, handler)
            watch(sessionManager.getActiveSessions(access.component))
        } catch (_: SecurityException) {
            // Access was revoked between the check and the call; the recheck flips `granted`.
            access.recheck()
            trySend(MediaState.NoAccess)
        }
        awaitClose {
            sessionManager.removeOnActiveSessionsChangedListener(listener)
            watched.forEach { (controller, callback) -> controller.unregisterCallback(callback) }
            current = null
        }
    }

    /** The system orders sessions by priority; one that is actually playing wins over a paused one. */
    private fun pick(controllers: List<MediaController>): MediaController? =
        controllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING && it.hasTitle() }
            ?: controllers.firstOrNull { it.hasTitle() }

    private fun MediaController.hasTitle() = metadata?.title() != null

    private fun describe(controller: MediaController, art: ArtMemo): PlayingMedia? {
        val metadata = controller.metadata ?: return null
        return metadata.title()?.let { title -> describe(controller, metadata, title, art) }
    }

    private fun describe(
        controller: MediaController,
        metadata: MediaMetadata,
        title: String,
        art: ArtMemo,
    ): PlayingMedia {
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
            progress = playback?.let {
                playbackProgress(
                    positionMs = it.position,
                    durationMs = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION),
                    speed = it.playbackSpeed,
                    updatedAtMs = it.lastPositionUpdateTime,
                    isPlaying = isPlaying,
                )
            },
        )
    }

    private fun MediaMetadata.title(): String? =
        getString(MediaMetadata.METADATA_KEY_TITLE) ?: getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
