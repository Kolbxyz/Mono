package com.wander.mono.core.usage

/** One of an app's activities coming to the front ([resumed]) or leaving it, at [time] ms. */
data class ActivityMark(val activity: String, val resumed: Boolean, val time: Long)

/**
 * How long the app had at least one activity in front between [start] and [end], from its
 * activity [marks] in time order. An activity that leaves the front before any came to it was
 * already there at [start]; one still in front at the end counts until [end].
 */
fun foregroundMillis(marks: List<ActivityMark>, start: Long, end: Long): Long {
    val front = mutableSetOf<String>()
    var anyResumed = false
    var since = start
    var total = 0L
    for (mark in marks) {
        val wasInFront = front.isNotEmpty()
        when {
            mark.resumed -> {
                front += mark.activity
                anyResumed = true
            }
            mark.activity in front -> front -= mark.activity
            !anyResumed -> total = maxOf(total, mark.time - start)
        }
        if (!wasInFront && front.isNotEmpty()) {
            since = mark.time
        } else if (wasInFront && front.isEmpty()) {
            total += mark.time - since
        }
    }
    if (front.isNotEmpty()) total += end - since
    return total.coerceAtLeast(0)
}
