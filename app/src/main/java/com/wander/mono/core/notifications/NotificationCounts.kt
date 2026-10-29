package com.wander.mono.core.notifications

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Per-app notification counts, published by [MonoNotificationListener] on every change. */
@Singleton
class NotificationCounts @Inject constructor() {
    private val mutableCounts = MutableStateFlow<Map<PackageUser, Int>>(emptyMap())
    val counts: StateFlow<Map<PackageUser, Int>> = mutableCounts.asStateFlow()

    internal fun publish(counts: Map<PackageUser, Int>) {
        mutableCounts.value = counts
    }
}
