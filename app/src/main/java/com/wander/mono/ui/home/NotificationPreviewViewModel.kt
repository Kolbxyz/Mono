package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import com.wander.mono.R
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.notifications.NotificationActions
import com.wander.mono.core.notifications.PreviewAction
import com.wander.mono.core.notifications.PreviewConversation
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * What the preview rows do: expand, open, dismiss, reply and act. Only one row is open at a time
 * (spec 4.4), whether expanded or replying: opening one closes any other.
 */
@HiltViewModel
class NotificationPreviewViewModel @Inject constructor(
    private val notifications: NotificationActions,
    private val apps: AppActions,
    private val messages: UserMessages,
) : ViewModel() {
    private val mutableReplying = MutableStateFlow<String?>(null)

    /** The [NotificationRow.key] whose reply field is open. */
    val replying: StateFlow<String?> = mutableReplying.asStateFlow()

    private val mutableExpanded = MutableStateFlow<String?>(null)

    /** The [NotificationRow.key] showing its conversations and actions. */
    val expanded: StateFlow<String?> = mutableExpanded.asStateFlow()

    /** The notification's own tap target, or the app when the notification has none. */
    fun open(row: NotificationRow) {
        messages.report(notifications.open(row.preview) ?: apps.launch(row.app.key))
    }

    /** One conversation's deep link, or the app when it has none. */
    fun open(row: NotificationRow, conversation: PreviewConversation) {
        messages.report(notifications.open(conversation) ?: apps.launch(row.app.key))
    }

    /** False when it could not be dismissed, so the row comes back. */
    fun dismiss(row: NotificationRow): Boolean = messages.report(notifications.dismiss(row.preview))

    fun setExpanded(row: NotificationRow, open: Boolean) {
        mutableExpanded.value = if (open) row.key else mutableExpanded.value.takeIf { it != row.key }
        if (open && mutableReplying.value != row.key) mutableReplying.value = null
    }

    fun setReplying(row: NotificationRow, open: Boolean) {
        mutableReplying.value = if (open) row.key else mutableReplying.value.takeIf { it != row.key }
        if (open && mutableExpanded.value != row.key) mutableExpanded.value = null
    }

    fun fire(action: PreviewAction) {
        messages.report(notifications.fire(action))
    }

    fun markRead(row: NotificationRow) {
        if (messages.report(notifications.markRead(row.preview))) setExpanded(row, open = false)
    }

    fun mute(row: NotificationRow) {
        if (messages.report(notifications.mute(row.preview))) {
            setExpanded(row, open = false)
            messages.show(R.string.preview_muted)
        }
    }

    /** True once sent, so the field can clear and close. */
    fun reply(row: NotificationRow, text: String): Boolean {
        val sent = messages.report(notifications.reply(row.preview, text))
        if (sent) setReplying(row, open = false)
        return sent
    }
}
