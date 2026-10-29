package com.wander.mono.core.usage

/** One of an app's activities coming to the front ([resumed]) or leaving it, at [time] ms. */
data class ActivityMark(val activity: String, val resumed: Boolean, val time: Long)
