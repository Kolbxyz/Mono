package com.wander.mono.core.notifications

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Each app's latest notification and the unread count of each conversation, published by
 * [MonoNotificationListener] on every change. Dismissing goes back through the bound listener,
 * the only thing allowed to cancel another app's notification.
 */
@Singleton
class NotificationPreviews @Inject constructor() {
    private val mutablePreviews = MutableStateFlow<Map<PackageUser, NotificationPreview>>(emptyMap())
    val previews: StateFlow<Map<PackageUser, NotificationPreview>> = mutablePreviews.asStateFlow()

    private val mutableConversations = MutableStateFlow<Map<ConversationKey, Int>>(emptyMap())
    val conversations: StateFlow<Map<ConversationKey, Int>> = mutableConversations.asStateFlow()

    /** Set while the listener is bound. */
    private var canceller: ((String) -> Unit)? = null

    internal fun publish(previews: Map<PackageUser, NotificationPreview>, conversations: Map<ConversationKey, Int>) {
        mutablePreviews.value = previews
        mutableConversations.value = conversations
    }

    internal fun bind(cancel: ((String) -> Unit)?) {
        canceller = cancel
    }

    /** Cancels the notification [key], as swiping it away in the shade does. False when unbound. */
    fun cancel(key: String): Boolean {
        val cancel = canceller ?: return false
        cancel(key)
        return true
    }
}
