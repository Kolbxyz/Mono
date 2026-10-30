package com.wander.mono.data

import com.wander.mono.core.apps.AppKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SwipeActionTest {
    private val key = AppKey("org.chat", "org.chat.Main", 10)

    @Test
    fun `every action survives with its app`() {
        listOf(SwipeAction.AppInfo, SwipeAction.Shortcut("room|42/x:y")).forEach { action ->
            assertEquals(key to action, SwipeEntry.decode(SwipeEntry.encode(key, action)))
        }
    }

    @Test
    fun `stored values never change`() {
        assertEquals("org.chat/org.chat.Main/10|info", SwipeEntry.encode(key, SwipeAction.AppInfo))
        assertEquals("shortcut:compose", SwipeAction.Shortcut("compose").encode())
    }

    @Test
    fun `unknown or damaged entries are skipped`() {
        assertNull(SwipeEntry.decode("org.chat/org.chat.Main/10|wave"))
        assertNull(SwipeEntry.decode("org.chat/org.chat.Main/10|shortcut:"))
        assertNull(SwipeEntry.decode("no key|info"))
        assertNull(SwipeEntry.decode("org.chat/org.chat.Main/10"))
    }
}
