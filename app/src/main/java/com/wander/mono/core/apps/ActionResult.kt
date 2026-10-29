package com.wander.mono.core.apps

import androidx.annotation.StringRes

/** Outcome of asking the system to open something. A failure carries the message to show. */
sealed interface ActionResult {
    data object Done : ActionResult

    data class Failed(@StringRes val message: Int) : ActionResult
}
