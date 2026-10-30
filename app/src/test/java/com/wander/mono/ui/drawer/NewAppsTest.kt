package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NewAppsTest {
    private val day = TimeUnit.DAYS.toMillis(1)
    private val now = 100 * day

    private fun app(installedAt: Long?, archived: Boolean = false) = LaunchableApp(
        AppKey("org.app", "org.app.Main", 0),
        "App",
        isMainProfile = true,
        installedAt = installedAt,
        archived = archived,
    )

    @Test
    fun `new for three days after install, until opened`() {
        assertTrue(isNew(app(now - day), now, emptySet()))
        assertTrue(isNew(app(now - 3 * day + 1), now, emptySet()))
        assertFalse(isNew(app(now - 3 * day), now, emptySet()))
        val fresh = app(now - day)
        assertFalse(isNew(fresh, now, setOf(fresh.key)))
    }

    @Test
    fun `no install time, an archived app or a future install time is never new`() {
        assertFalse(isNew(app(null), now, emptySet()))
        assertFalse(isNew(app(now - day, archived = true), now, emptySet()))
        assertFalse(isNew(app(now + day), now, emptySet()))
    }

    @Test
    fun `the next boundary is the earliest app to stop being new`() {
        assertEquals(now + day, nextNewExpiry(listOf(now - 2 * day, now - day), now))
        assertNull(nextNewExpiry(listOf(now - 5 * day), now))
        assertNull(nextNewExpiry(emptyList(), now))
    }
}
