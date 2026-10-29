package com.wander.mono.core.media

import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState

/** One item of the session's queue, as the "up next" list shows it. [id] is the session's queue id. */
data class QueueEntry(val id: Long, val title: String, val subtitle: String?)

/**
 * The [max] entries right after [activeId] in [queue]. When the session does not say which item is
 * playing, or names one that is not in the queue, nothing is guessed: the list is empty.
 */
internal fun upNext(queue: List<QueueEntry>, activeId: Long, max: Int = UP_NEXT_MAX): List<QueueEntry> {
    val active = queue.indexOfFirst { it.id == activeId }
    return if (activeId == MediaSession.QueueItem.UNKNOWN_ID.toLong() || active < 0) {
        emptyList()
    } else {
        queue.drop(active + 1).take(max)
    }
}

/**
 * What the session plays next, read from its published queue. Media3 sessions (Wanda among them)
 * publish their timeline as the queue, with the item index as the id. Empty when the session
 * cannot jump to a queue item, since a list that cannot be tapped would only be noise.
 */
internal fun MediaController.upNext(): List<QueueEntry> {
    val state = playbackState
    if (state == null || state.actions and PlaybackState.ACTION_SKIP_TO_QUEUE_ITEM == 0L) return emptyList()
    val entries = queue.orEmpty().mapNotNull { item ->
        item.description.title?.toString()?.let { title ->
            QueueEntry(item.queueId, title, item.description.subtitle?.toString())
        }
    }
    return upNext(entries, state.activeQueueItemId)
}

/** Enough to see what is coming without turning the card into a playlist. */
private const val UP_NEXT_MAX = 3
