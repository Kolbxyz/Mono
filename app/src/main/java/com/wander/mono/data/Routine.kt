package com.wander.mono.data

import com.wander.mono.core.apps.AppKey
import java.time.LocalTime

/**
 * A daily window, from [start] to [end] in minutes after midnight, during which [apps] go first on
 * home. A window whose end comes before its start runs past midnight; one that ends where it
 * starts is never on.
 */
data class Routine(val start: Int, val end: Int, val apps: Set<AppKey>) {
    fun isOn(minute: Int): Boolean = when {
        start < end -> minute in start until end
        start > end -> minute >= start || minute < end
        else -> false
    }

    /** One line: start, end, then the app keys, comma-separated (keys never hold '|' or ','). */
    fun encode(): String = "$start$FIELD$end$FIELD${apps.joinToString(APPS.toString()) { it.encode() }}"

    companion object {
        /** At most this many routines: enough for morning, day and evening. */
        const val MAX = 3
        const val MINUTES_PER_HOUR = 60
        const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR
        private const val FIELD = '|'
        private const val APPS = ','
        private const val FIELDS = 3

        fun decode(encoded: String): Routine? {
            val fields = encoded.split(FIELD).takeIf { it.size == FIELDS } ?: return null
            val start = fields[0].toIntOrNull()?.takeIf(::isMinuteOfDay)
            val end = fields[1].toIntOrNull()?.takeIf(::isMinuteOfDay)
            val apps = fields[2].split(APPS).filter { it.isNotEmpty() }.map(AppKey::decode)
            return if (start != null && end != null && null !in apps) {
                Routine(start, end, apps.filterNotNull().toSet())
            } else {
                null
            }
        }

        fun isMinuteOfDay(minute: Int): Boolean = minute in 0 until MINUTES_PER_DAY
    }
}

/** The apps of every routine on at [now]. */
fun List<Routine>.appsAt(now: LocalTime): Set<AppKey> {
    val minute = now.hour * Routine.MINUTES_PER_HOUR + now.minute
    return filter { it.isOn(minute) }.flatMapTo(mutableSetOf()) { it.apps }
}

/**
 * Milliseconds from [now] to the next start or end of any routine, so home re-sorts once, right
 * then, instead of checking on a timer; null when no routine ever changes.
 */
fun List<Routine>.millisToNextBoundary(now: LocalTime): Long? {
    val nowMillis = now.toNanoOfDay() / NANOS_PER_MILLI
    return filter { it.start != it.end }
        .flatMap { listOf(it.start, it.end) }
        .map { minute ->
            val until = minute * MILLIS_PER_MINUTE - nowMillis
            if (until > 0) until else until + Routine.MINUTES_PER_DAY * MILLIS_PER_MINUTE
        }
        .minOrNull()
}

private const val NANOS_PER_MILLI = 1_000_000L
private const val MILLIS_PER_MINUTE = 60_000L
