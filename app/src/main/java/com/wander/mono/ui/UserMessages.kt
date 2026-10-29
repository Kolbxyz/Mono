package com.wander.mono.ui

import androidx.annotation.StringRes
import com.wander.mono.core.apps.ActionResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Errors and short confirmations any screen reports, shown once as a snackbar by the root. */
@Singleton
class UserMessages @Inject constructor() {
    private val mutableMessages = MutableSharedFlow<Int>(extraBufferCapacity = BUFFER)
    val messages: SharedFlow<Int> = mutableMessages.asSharedFlow()

    /** Shows a failed [result]'s message; true when the action went through. */
    fun report(result: ActionResult): Boolean = when (result) {
        ActionResult.Done -> true
        is ActionResult.Failed -> {
            show(result.message)
            false
        }
    }

    /** Shows [message] once, for confirmations as well as errors. */
    fun show(@StringRes message: Int) {
        mutableMessages.tryEmit(message)
    }

    private companion object {
        const val BUFFER = 4
    }
}
