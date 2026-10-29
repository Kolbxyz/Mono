package com.wander.mono.ui.sheet

/** Today's time in an app, as whole hours and minutes. */
sealed interface UsageTime {
    data object UnderAMinute : UsageTime

    data class Minutes(val minutes: Int) : UsageTime

    data class Hours(val hours: Int, val minutes: Int) : UsageTime
}

fun usageTime(millis: Long): UsageTime {
    val totalMinutes = (millis / MILLIS_PER_MINUTE).toInt()
    return when {
        totalMinutes < 1 -> UsageTime.UnderAMinute
        totalMinutes < MINUTES_PER_HOUR -> UsageTime.Minutes(totalMinutes)
        else -> UsageTime.Hours(totalMinutes / MINUTES_PER_HOUR, totalMinutes % MINUTES_PER_HOUR)
    }
}

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60
