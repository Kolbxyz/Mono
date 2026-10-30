package com.wander.mono.core.notifications

import android.service.notification.NotificationListenerService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Each app's latest notification and the unread count of each conversation, published by
 * [MonoNotificationListener] on every change. Dismissing goes back through the bound listener,
 * the only thing allowed to cancel or snooze another app's notification.
 */
@Singleton
class NotificationPreviews @Inject constructor() {
    private val mutablePreviews = MutableStateFlow<Map<PackageUser, NotificationPreview>>(emptyMap())
    val previews: StateFlow<Map<PackageUser, NotificationPreview>> = mutablePreviews.asStateFlow()

    private val mutableConversations = MutableStateFlow<Map<ConversationKey, Int>>(emptyMap())
    val conversations: StateFlow<Map<ConversationKey, Int>> = mutableConversations.asStateFlow()

    /** Set while the listener is bound. */
    private var listener: NotificationListenerService? = null

    internal fun publish(previews: Map<PackageUser, NotificationPreview>, conversations: Map<ConversationKey, Int>) {
        mutablePreviews.value = previews
        mutableConversations.value = conversations
    }

    internal fun bind(listener: NotificationListenerService?) {
        this.listener = listener
    }

    /** Cancels the notification [key], as swiping it away in the shade does. False when unbound. */
    fun cancel(key: String): Boolean {
        val bound = listener ?: return false
        bound.cancelNotification(key)
        return true
    }

    /** Hides the notification [key] for [durationMs], as snoozing it in the shade does. False when unbound. */
    fun snooze(key: String, durationMs: Long): Boolean {
        val bound = listener ?: return false
        bound.snoozeNotification(key, durationMs)
        return true
    }
}
