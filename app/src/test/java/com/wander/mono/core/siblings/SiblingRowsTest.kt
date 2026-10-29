package com.wander.mono.core.siblings

import com.wander.mono.core.media.QueueEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SiblingRowsTest {
    /** A cursor row as a map: a missing key is a missing column. */
    private class FakeRow(private val values: Map<String, Any?>) : ProviderRow {
        override fun string(column: String): String? = values[column]?.toString()

        override fun long(column: String): Long? = (values[column] as Number?)?.toLong()
    }

    private fun row(vararg pairs: Pair<String, Any?>) = FakeRow(mapOf(*pairs))

    @Test
    fun `an up next row keeps its queue id and uses the artist as subtitle`() {
        val entry = SiblingRows.upNext(row("_id" to 0L, "queue_id" to 7L, "title" to "Song", "artist" to "Band"))
        assertEquals(QueueEntry(7L, "Song", "Band"), entry)
    }

    @Test
    fun `an up next row without queue id or title is dropped`() {
        assertNull(SiblingRows.upNext(row("title" to "Song")))
        assertNull(SiblingRows.upNext(row("queue_id" to 1L, "title" to " ")))
        assertEquals(QueueEntry(1L, "Song", null), SiblingRows.upNext(row("queue_id" to 1L, "title" to "Song")))
    }

    @Test
    fun `unknown columns are ignored`() {
        val track = SiblingRows.track(
            row("track_id" to "t1", "title" to "Song", "artist" to "Band", "play_uri" to "wanda://track?id=t1", "x" to 1),
            WANDA,
        )
        assertEquals(WandaTrack("t1", "Song", "Band", "wanda://track?id=t1", WANDA), track)
    }

    @Test
    fun `a track that cannot be played is not offered`() {
        assertNull(SiblingRows.track(row("track_id" to "t1", "title" to "Song"), WANDA))
        assertNull(SiblingRows.track(row("title" to "Song", "play_uri" to "wanda://track"), WANDA))
    }

    @Test
    fun `rooms carry their unread count and marked-unread flag`() {
        assertEquals(RoomUnread("!a:x", 3, false), SiblingRows.room(row("room_id" to "!a:x", "unread_count" to 3L)))
        assertEquals(
            RoomUnread("!b:x", 0, true),
            SiblingRows.room(row("room_id" to "!b:x", "unread_count" to 0L, "is_marked_unread" to 1L)),
        )
    }

    @Test
    fun `a room with nothing unread or no id is dropped`() {
        assertNull(SiblingRows.room(row("room_id" to "!a:x", "unread_count" to 0L, "is_marked_unread" to 0L)))
        assertNull(SiblingRows.room(row("unread_count" to 4L)))
    }

    @Test
    fun `a snapshot older than a day is stale`() {
        val day = 24 * 60 * 60 * 1000L
        assertTrue(SiblingRows.isFresh(null, nowMs = 10 * day))
        assertTrue(SiblingRows.isFresh(9 * day, nowMs = 10 * day))
        assertFalse(SiblingRows.isFresh(8 * day, nowMs = 10 * day))
    }

    private companion object {
        const val WANDA = "com.wander.android"
    }
}
