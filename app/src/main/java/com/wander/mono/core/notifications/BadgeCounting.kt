package com.wander.mono.core.notifications

/**
 * Notifications that deserve a dot, per app. Ongoing ones (music, downloads, VPN) are status, not
 * news, and a group summary duplicates its children. The user's per-channel "Allow notification
 * dot" setting is honoured through [BadgeCandidate.canShowBadge].
 */
internal fun countBadges(candidates: List<BadgeCandidate>): Map<PackageUser, Int> = candidates
    .filter { it.canShowBadge && !it.isOngoing && !it.isGroupSummary }
    .groupingBy { it.owner }
    .eachCount()
