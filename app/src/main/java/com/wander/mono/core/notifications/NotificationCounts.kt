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

    private val mutableRead = MutableStateFlow<Map<PackageUser, Int>?>(null)

    /**
     * [counts] as the listener last read them; null while it is not connected (before it binds, or
     * after it lost access), so whoever compares two readings never mistakes the listener coming
     * back for every notification arriving at once.
     */
    val read: StateFlow<Map<PackageUser, Int>?> = mutableRead.asStateFlow()

    internal fun publish(counts: Map<PackageUser, Int>) {
        mutableCounts.value = counts
        mutableRead.value = counts
    }

    /** The listener disconnected: no counts to show, and none read until it connects again. */
    internal fun disconnected() {
        mutableCounts.value = emptyMap()
        mutableRead.value = null
    }
}
