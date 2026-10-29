package com.wander.mono.core.notifications

/** What [countBadges] needs to know about one active notification. */
internal data class BadgeCandidate(
    val owner: PackageUser,
    val isOngoing: Boolean,
    val isGroupSummary: Boolean,
    val canShowBadge: Boolean,
)
