package com.wander.mono.data

import com.wander.mono.core.apps.AppKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LimitEntryTest {
    private val key = AppKey("org.video", "org.video.Main", 0)

    @Test
    fun `a limit survives with its app`() {
        assertEquals(key to 45, LimitEntry.decode(LimitEntry.encode(key, 45)))
    }

    @Test
    fun `only whole minutes above zero are limits`() {
        assertNull(LimitEntry.decode("org.video/org.video.Main/0|0"))
        assertNull(LimitEntry.decode("org.video/org.video.Main/0|half"))
        assertNull(LimitEntry.decode("org.video/org.video.Main/0"))
        assertNull(LimitEntry.decode("nothing|30"))
    }
}
