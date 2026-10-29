package com.wander.mono.core.media

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
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
    private val outputs: AudioOutputs,
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

    fun seekTo(positionMs: Long) {
        current?.transportControls?.seekTo(positionMs)
    }

    fun skipToQueueItem(id: Long) {
        current?.transportControls?.skipToQueueItem(id)
    }

    fun openOutputSwitcher() {
        current?.let { outputs.openSwitcher(it.packageName) }
    }

    private fun sessions(): Flow<MediaState> = callbackFlow {
        val handler = Handler(Looper.getMainLooper())
        val watched = mutableMapOf<MediaController, MediaController.Callback>()
        val art = ArtMemo()

        fun publish() {
            val chosen = pick(watched.keys.toList())
            current = chosen
            val media = chosen?.let { playingMedia(it, art, outputs.current(it)) }
            trySend(media?.let(MediaState::Active) ?: MediaState.Idle)
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
                    override fun onQueueChanged(queue: List<MediaSession.QueueItem>?) = publish()
                    override fun onAudioInfoChanged(info: MediaController.PlaybackInfo) = publish()
                }
                controller.registerCallback(callback, handler)
                watched[controller] = callback
            }
            publish()
        }

        val listener = MediaSessionManager.OnActiveSessionsChangedListener { watch(it.orEmpty()) }
        val unwatchOutputs = outputs.watch(::publish)
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
            unwatchOutputs()
            watched.forEach { (controller, callback) -> controller.unregisterCallback(callback) }
            current = null
        }
    }

    /** The system orders sessions by priority; one that is actually playing wins over a paused one. */
    private fun pick(controllers: List<MediaController>): MediaController? =
        controllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING && it.hasTitle() }
            ?: controllers.firstOrNull { it.hasTitle() }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
