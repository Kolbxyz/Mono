package com.wander.mono.ui.home

import com.wander.mono.core.calendar.CalendarEvent

/** What the glance header shows: the current time and the next alarm (epoch millis), and the next event. */
data class Glance(val now: Long, val nextAlarm: Long?, val nextEvent: CalendarEvent? = null)
