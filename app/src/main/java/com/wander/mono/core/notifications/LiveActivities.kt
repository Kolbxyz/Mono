package com.wander.mono.core.notifications

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The ongoing notifications shown as live rows, published by [MonoNotificationListener] on every change. */
@Singleton
class LiveActivities @Inject constructor() {
    private val mutableActivities = MutableStateFlow<List<LiveActivity>>(emptyList())
    val activities: StateFlow<List<LiveActivity>> = mutableActivities.asStateFlow()

    internal fun publish(activities: List<LiveActivity>) {
        mutableActivities.value = activities
    }
}
