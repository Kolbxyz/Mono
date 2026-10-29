package com.wander.mono.ui.home

/** What the glance header shows: the current time and the next alarm, both epoch millis. */
data class Glance(val now: Long, val nextAlarm: Long?)
