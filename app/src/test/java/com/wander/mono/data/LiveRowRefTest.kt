package com.wander.mono.data

import com.wander.mono.core.apps.AppKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        assertEquals("now_playing|com.wander.android/com.wander.android.Main/10", LiveRowRef(key, LiveRowKind.NowPlaying).encode())
    }

    @Test
    fun `unknown kinds and broken keys decode to null`() {
        assertNull(LiveRowRef.decode("widget|com.wander.android/com.wander.android.Main/10"))
        assertNull(LiveRowRef.decode("now_playing|com.wander.android"))
        assertNull(LiveRowRef.decode(""))
    }
}
