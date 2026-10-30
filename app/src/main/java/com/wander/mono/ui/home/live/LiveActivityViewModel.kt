package com.wander.mono.ui.home.live

import androidx.lifecycle.ViewModel
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.notifications.NotificationActions
import com.wander.mono.core.notifications.PreviewAction
import com.wander.mono.ui.UserMessages
import com.wander.mono.ui.home.LiveActivityRow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** What a live row does: open its notification's target, or fire one of its buttons. */
@HiltViewModel
class LiveActivityViewModel @Inject constructor(
    private val notifications: NotificationActions,
    private val apps: AppActions,
    private val messages: UserMessages,
) : ViewModel() {
    /** The notification's own tap target, or the app when the notification has none. */
    fun open(row: LiveActivityRow) {
        messages.report(notifications.open(row.activity) ?: apps.launch(row.app.key))
    }

    fun fire(action: PreviewAction) {
        messages.report(notifications.fire(action))
    }
}
