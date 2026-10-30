package com.wander.mono.core.notifications

import androidx.core.app.NotificationCompat

/** What [liveKindOf] reads from one active notification. */
internal data class LiveTraits(
    val category: String?,
    val isOngoing: Boolean,
    val isGroupSummary: Boolean,
    /** It carries a media session: the player under the clock already shows it. */
    val isMedia: Boolean,
    val showsChronometer: Boolean,
    /** Posted by an app that takes timers (the clock app), whose ongoing notifications are timers. */
    val fromClockApp: Boolean,
    /** Android 16's `ProgressStyle`, or the app asked for its notification to be a promoted Live Update. */
    val isLiveUpdate: Boolean,
    val progress: LiveProgress?,
)

/**
 * The live row an active notification becomes, null for none. Only ongoing notifications qualify,
 * never media (the player under the clock) or calls. Navigation goes by its category; a Live Update with a
 * progress is a progress row even when it also runs a chronometer; a chronometer, the stopwatch
 * category or the clock app make a timer; anything else needs a determinate progress (or the
 * progress category, or to be a Live Update) to count as under way.
 */
internal fun liveKindOf(traits: LiveTraits): LiveActivityKind? = with(traits) {
    when {
        !isOngoing || isGroupSummary || isMedia || category in NOT_LIVE -> null
        category == NotificationCompat.CATEGORY_NAVIGATION -> LiveActivityKind.Navigation
        isLiveUpdate && progress != null -> LiveActivityKind.Progress
        isTimer() -> LiveActivityKind.Timer
        isUnderWay() -> LiveActivityKind.Progress
        else -> null
    }
}

private fun LiveTraits.isTimer(): Boolean =
    showsChronometer || fromClockApp || category == NotificationCompat.CATEGORY_STOPWATCH

private fun LiveTraits.isUnderWay(): Boolean =
    isLiveUpdate || progress is LiveProgress.Determinate ||
        (category == NotificationCompat.CATEGORY_PROGRESS && progress != null)

/** The progress bar from the notification's `EXTRA_PROGRESS*` extras; none when its maximum is 0. */
internal fun progressOf(value: Int, max: Int, indeterminate: Boolean): LiveProgress? = when {
    indeterminate -> LiveProgress.Indeterminate
    max > 0 -> LiveProgress.Determinate((value.toFloat() / max).coerceIn(0f, 1f))
    else -> null
}

/** One classified notification, before it is read in full. */
internal data class LiveCandidate(
    val key: String,
    val owner: PackageUser,
    val kind: LiveActivityKind,
    /** Its chronometer is running. */
    val ticking: Boolean,
    /** `Notification.when`, which an update keeps (unlike the post time). */
    val startedAt: Long,
)

/**
 * One live row per app and kind: a running timer over a paused one, then the one started first, so
 * updates of several downloads never swap the one shown.
 */
internal fun chooseLive(candidates: List<LiveCandidate>): List<LiveCandidate> = candidates
    .groupBy { it.owner to it.kind }
    .values
    .map { group ->
        group.minWith(compareByDescending<LiveCandidate> { it.ticking }.thenBy { it.startedAt }.thenBy { it.key })
    }

/** Media transport (the player under the clock) and calls are never live rows. */
private val NOT_LIVE = setOf(NotificationCompat.CATEGORY_TRANSPORT, NotificationCompat.CATEGORY_CALL)
