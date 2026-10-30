package com.wander.mono.ui

import androidx.annotation.StringRes
import com.wander.mono.core.apps.ActionResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** One snackbar: its text, whether it reports a failure (felt as well as read), and what it offers to do. */
data class UserMessage(@StringRes val text: Int, val isError: Boolean, val action: MessageAction? = null)

/** A snackbar's button, such as Undo, and what it does. */
class MessageAction(@StringRes val label: Int, val onAction: () -> Unit)

/** Errors and short confirmations any screen reports, shown once as a snackbar by the root. */
@Singleton
class UserMessages @Inject constructor() {
    private val mutableMessages = MutableSharedFlow<UserMessage>(extraBufferCapacity = BUFFER)
    val messages: SharedFlow<UserMessage> = mutableMessages.asSharedFlow()

    /** Shows a failed [result]'s message; true when the action went through. */
    fun report(result: ActionResult): Boolean = when (result) {
        ActionResult.Done -> true
        is ActionResult.Failed -> {
            mutableMessages.tryEmit(UserMessage(result.message, isError = true))
            false
        }
    }

    /** Shows [message] once, for confirmations, with [action] as its button when given. */
    fun show(@StringRes message: Int, action: MessageAction? = null) {
        mutableMessages.tryEmit(UserMessage(message, isError = false, action = action))
    }

    private companion object {
        const val BUFFER = 4
    }
}
