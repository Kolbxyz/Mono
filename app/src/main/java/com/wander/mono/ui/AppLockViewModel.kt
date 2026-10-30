package com.wander.mono.ui

import androidx.lifecycle.ViewModel
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.security.AppLock
import com.wander.mono.core.security.LaunchRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** The launches [AppLock] held back, from wherever they were asked for, and running them once confirmed. */
@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val lock: AppLock,
    private val actions: AppActions,
    private val messages: UserMessages,
) : ViewModel() {
    val requests: Flow<LaunchRequest> = lock.requests

    suspend fun mustConfirm(request: LaunchRequest): Boolean = lock.mustConfirm(request)

    /** Without asking: the app turned out not to be locked. */
    fun launch(request: LaunchRequest) {
        messages.report(actions.launch(request))
    }

    fun unlockAndLaunch(request: LaunchRequest) {
        lock.unlock(request)
        launch(request)
    }

    fun reportNoScreenLock() {
        messages.report(ActionResult.Failed(R.string.app_lock_no_screen_lock))
    }
}
