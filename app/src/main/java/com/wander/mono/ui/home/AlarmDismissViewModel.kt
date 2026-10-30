package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.system.AlarmDismiss
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The glance header's long press on the next-alarm line: dismiss it in the clock app, or say why not. */
@HiltViewModel
class AlarmDismissViewModel @Inject constructor(
    private val alarms: AlarmDismiss,
    private val messages: UserMessages,
) : ViewModel() {
    fun dismissNext() {
        viewModelScope.launch {
            // Resolving the clock app's activity reads the package manager: off the main thread.
            val result = withContext(Dispatchers.IO) { alarms.dismissNext() }
            messages.report(result)
        }
    }
}
