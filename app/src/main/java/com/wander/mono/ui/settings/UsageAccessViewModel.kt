package com.wander.mono.ui.settings

import androidx.lifecycle.ViewModel
import com.wander.mono.core.usage.AppUsage
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Whether usage access is granted, re-read when settings come back to the screen (no event exists). */
@HiltViewModel
class UsageAccessViewModel @Inject constructor(
    private val usage: AppUsage,
    private val messages: UserMessages,
) : ViewModel() {
    private val mutableGranted = MutableStateFlow(usage.isGranted())
    val granted: StateFlow<Boolean> = mutableGranted.asStateFlow()

    fun recheck() {
        mutableGranted.value = usage.isGranted()
    }

    fun openSettings() {
        messages.report(usage.openSettings())
    }
}
