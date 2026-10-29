package com.wander.mono.core.notifications

/**
 * Unread items per app. Ongoing notifications (music, downloads, VPN) are status, not news, and a
 * group summary duplicates its children. The user's per-channel "Allow notification dot" setting is
 * honoured through [BadgeCandidate.canShowBadge]. A notification counts for its `number` when the
 * app sets one (a chat bundling 4 messages), else for 1, as the platform launcher does.
 */
internal fun countBadges(candidates: List<BadgeCandidate>): Map<PackageUser, Int> = candidates
    .filter { it.canShowBadge && !it.isOngoing && !it.isGroupSummary }
    .groupingBy { it.owner }
    .fold(0) { total, candidate -> total + candidate.number.coerceAtLeast(1) }
