package com.wander.mono.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.data.OnboardingPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The one-time "swipe up for all apps" chip: shown after setup until the drawer is first opened. */
@HiltViewModel
class SwipeHintViewModel @Inject constructor(private val prefs: OnboardingPrefs) : ViewModel() {
    /** False until read, so the chip never shows a frame to someone who already knows. */
    val shown: StateFlow<Boolean> = prefs.showsSwipeHint.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun drawerOpened() {
        if (!shown.value) return
        viewModelScope.launch { prefs.markDrawerOpened() }
    }
}
