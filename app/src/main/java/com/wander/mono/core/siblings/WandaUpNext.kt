package com.wander.mono.core.siblings

import android.net.Uri
import com.wander.mono.core.media.MediaState
import com.wander.mono.core.media.QueueEntry
import com.wander.mono.core.siblings.SiblingContract.Wanda
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Wanda's own "up next", in true playback order, for the now-playing card when Wanda plays and its
 * session publishes no usable queue. Wanda's provider is watched only while that is the case.
 */
@Singleton
class WandaUpNext @Inject constructor(private val providers: SiblingProviders) {
    /** [state], with Wanda's up-next filled in where the session's own queue left it empty. */
    fun fill(state: Flow<MediaState>): Flow<MediaState> {
        val items = state.map(::wandaNeedingQueue)
            .distinctUntilChanged()
            .flatMapLatest { packageName -> packageName?.let(::upNext) ?: flowOf(emptyList()) }
        return combine(state, items) { current, wanda ->
            if (wanda.isNotEmpty() && wandaNeedingQueue(current) != null) {
                current as MediaState.Active
                current.copy(media = current.media.copy(upNext = wanda))
            } else {
                current
            }
        }
    }

    /** The Wanda build that is playing with an empty queue, or null when nothing needs filling. */
    private fun wandaNeedingQueue(state: MediaState): String? {
        val media = (state as? MediaState.Active)?.media ?: return null
        return media.packageName.takeIf { it in Wanda.packages && media.upNext.isEmpty() }
    }

    private fun upNext(packageName: String): Flow<List<QueueEntry>> = flow {
        val authority = providers.authority(listOf(packageName))
        if (authority == null) {
            emit(emptyList())
        } else {
            val uri = Uri.Builder().scheme("content").authority(authority).path(Wanda.PATH_UP_NEXT)
                .appendQueryParameter(SiblingContract.PARAM_LIMIT, MAX.toString())
                .build()
            emitAll(providers.observe(uri) { it.mapRows(SiblingRows::upNext) }.map { it.orEmpty() })
        }
    }.flowOn(Dispatchers.IO)

    private companion object {
        /** As many as the card shows from the session's own queue. */
        const val MAX = 3
    }
}
