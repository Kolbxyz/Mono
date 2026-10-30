package com.wander.mono.data

import com.wander.mono.core.apps.AppKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveRowRefTest {
    private val key = AppKey("com.wander.android", "com.wander.android.Main", 10)

    @Test
    fun `round-trips every kind`() {
        LiveRowKind.entries.forEach { kind ->
            val ref = LiveRowRef(key, kind)
            assertEquals(ref, LiveRowRef.decode(ref.encode()))
        }
    }

    @Test
    fun `stored values never change`() {
        assertEquals(
            "notification_preview|com.wander.android/com.wander.android.Main/10",
            LiveRowRef(key, LiveRowKind.NotificationPreview).encode(),
        )
    }

    @Test
    fun `unknown kinds and broken keys decode to null`() {
        assertNull(LiveRowRef.decode("widget|com.wander.android/com.wander.android.Main/10"))
        assertNull(LiveRowRef.decode("calendar|com.wander.android"))
        assertNull(LiveRowRef.decode(""))
    }

    @Test
    fun `now playing refs from before it moved under the clock are retired, not decoded`() {
        val old = "now_playing|com.wander.android/com.wander.android.Main/10"
        assertTrue(LiveRowRef.isRetired(old))
        assertNull(LiveRowRef.decode(old))
        assertFalse(LiveRowRef.isRetired(LiveRowRef(key, LiveRowKind.Calendar).encode()))
        assertFalse(LiveRowRef.isRetired("weather|com.wander.android/com.wander.android.Main/10"))
    }
}
