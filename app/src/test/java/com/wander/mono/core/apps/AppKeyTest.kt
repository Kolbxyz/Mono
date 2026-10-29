package com.wander.mono.core.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppKeyTest {
    @Test
    fun `encodes and decodes, rejecting malformed keys`() {
        val key = AppKey("com.wander.android", "com.wander.android.MainActivity\$Alias", 10)
        assertEquals(key, AppKey.decode(key.encode()))
        assertNull(AppKey.decode("only/two"))
        assertNull(AppKey.decode("a/b/notanumber"))
    }
}
