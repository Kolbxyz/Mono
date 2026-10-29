package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.notifications.NotificationActions
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * What the preview rows do: open, dismiss and reply. Only one row has its reply field open at a
 * time; opening another closes the previous one.
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

    /** The notification's own tap target, or the app when the notification has none. */
    fun open(row: NotificationRow) {
        messages.report(notifications.open(row.preview) ?: apps.launch(row.app.key))
    }

    /** False when it could not be dismissed, so the row comes back. */
    fun dismiss(row: NotificationRow): Boolean = messages.report(notifications.dismiss(row.preview))

    fun setReplying(row: NotificationRow, open: Boolean) {
        mutableReplying.value = if (open) row.key else mutableReplying.value.takeIf { it != row.key }
    }

    /** True once sent, so the field can clear and close. */
    fun reply(row: NotificationRow, text: String): Boolean {
        val sent = messages.report(notifications.reply(row.preview, text))
        if (sent) setReplying(row, open = false)
        return sent
    }
}
