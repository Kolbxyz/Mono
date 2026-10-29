package com.wander.mono.core.siblings

import com.wander.mono.core.media.QueueEntry
import com.wander.mono.core.siblings.SiblingContract.Phalanx
import com.wander.mono.core.siblings.SiblingContract.Wanda

/** A song in Wanda's local library, as drawer search shows it. [playUri] is what Wanda opens. */
data class WandaTrack(val trackId: String, val title: String, val artist: String?, val playUri: String)

/** A Phalanx room with something unread. Its id is also the id of its conversation shortcut. */
data class RoomUnread(val roomId: String, val unread: Int, val markedUnread: Boolean)

/**
 * Cursor rows to Mono's models. A row missing a column Mono cannot do without is dropped rather
 * than shown half-filled; optional columns may be absent (an older sibling).
 */
internal object SiblingRows {
    /** Up-next rows keep their `queue_id`, so tapping one still skips the session to it. */
    fun upNext(row: ProviderRow): QueueEntry? {
        val id = row.long(Wanda.QUEUE_ID) ?: return null
        val title = row.string(Wanda.TITLE)?.takeIf { it.isNotBlank() } ?: return null
        return QueueEntry(id, title, row.string(Wanda.ARTIST)?.takeIf { it.isNotBlank() })
    }

    fun track(row: ProviderRow): WandaTrack? {
        val id = row.string(Wanda.TRACK_ID) ?: return null
        val title = row.string(Wanda.TITLE)?.takeIf { it.isNotBlank() } ?: return null
        val uri = row.string(Wanda.PLAY_URI)?.takeIf { it.isNotBlank() } ?: return null
        return WandaTrack(id, title, row.string(Wanda.ARTIST)?.takeIf { it.isNotBlank() }, uri)
    }

    fun room(row: ProviderRow): RoomUnread? {
        val id = row.string(Phalanx.ROOM_ID)?.takeIf { it.isNotBlank() } ?: return null
        val unread = (row.long(Phalanx.UNREAD_COUNT) ?: 0L).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        val marked = row.long(Phalanx.IS_MARKED_UNREAD) == 1L
        return RoomUnread(id, unread, marked).takeIf { unread > 0 || marked }
    }

    /**
     * Whether a snapshot taken at [snapshotAtMs] is recent enough to show. Older than a day, the
     * counts are more likely wrong than right, so Mono falls back to what notifications say. A
     * snapshot without the extra is taken as current.
     */
    fun isFresh(snapshotAtMs: Long?, nowMs: Long): Boolean =
        snapshotAtMs == null || snapshotAtMs <= 0L || nowMs - snapshotAtMs <= STALE_AFTER_MS

    private const val STALE_AFTER_MS = 24 * 60 * 60 * 1000L
}
