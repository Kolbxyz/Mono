package com.wander.mono.core.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class BadgeCountingTest {
    private val phalanx = PackageUser("org.phalanx.android", 0)
    private val wanda = PackageUser("com.wander.android", 0)

    private fun candidate(
        owner: PackageUser,
        ongoing: Boolean = false,
        summary: Boolean = false,
        badge: Boolean = true,
        number: Int = 0,
    ) = BadgeCandidate(owner, ongoing, summary, badge, number)

    @Test
    fun `ongoing, summaries and badge-disabled channels do not count`() {
        val counts = countBadges(
            listOf(
                candidate(phalanx),
                candidate(phalanx),
                candidate(phalanx, summary = true),
                candidate(wanda, ongoing = true),
                candidate(wanda, badge = false),
            )
        )
        assertEquals(mapOf(phalanx to 2), counts)
    }

    @Test
    fun `a notification counts for its number when the app sets one`() {
        val counts = countBadges(
            listOf(
                candidate(phalanx, number = 4),
                candidate(phalanx),
                candidate(phalanx, summary = true, number = 5),
            )
        )
        assertEquals(mapOf(phalanx to 5), counts)
    }
}
