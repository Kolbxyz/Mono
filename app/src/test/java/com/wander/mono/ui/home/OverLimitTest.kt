package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import org.junit.Assert.assertEquals
import org.junit.Test

class OverLimitTest {
    private val video = AppKey("org.video", "org.video.Main", 0)
    private val social = AppKey("org.social", "org.social.Main", 0)
    private val limits = mapOf(video to 30, social to 60)

    @Test
    fun `apps at or past their limit are over it`() {
        val today = mapOf("org.video" to 30 * MINUTE, "org.social" to 59 * MINUTE)
        assertEquals(setOf(video), overLimit(limits, today))
    }

    @Test
    fun `an app not used today is under its limit`() {
        assertEquals(emptySet<AppKey>(), overLimit(limits, emptyMap()))
    }

    @Test
    fun `without usage access nothing is over`() {
        assertEquals(emptySet<AppKey>(), overLimit(limits, null))
    }

    private companion object {
        const val MINUTE = 60_000L
    }
}
