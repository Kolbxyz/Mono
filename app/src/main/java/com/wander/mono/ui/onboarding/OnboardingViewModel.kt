package com.wander.mono.ui.onboarding

import android.content.Intent
import android.os.Build
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.notifications.ListenerAccess
import com.wander.mono.core.system.HomeRole
import com.wander.mono.data.OnboardingPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What the onboarding pane shows. */
data class OnboardingUi(
    val step: OnboardingStep,
    val canGoBack: Boolean,
    /** Access was asked for and is still off: likely Android's restricted-settings block. */
    val showRestrictedHelp: Boolean,
    @StringRes val error: Int?,
)

/**
 * First-run setup. The step the user is on survives rotation and process death; whether Mono is
 * default home and has notification access is read from the system, again whenever Mono returns
 * to the front, so a step done in a system screen is passed over on the way back.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val prefs: OnboardingPrefs,
    private val homeRole: HomeRole,
    private val listenerAccess: ListenerAccess,
    private val actions: AppActions,
) : ViewModel() {
    /** Null until read from disk, so home never flashes the setup pane, nor the reverse. */
    val done: StateFlow<Boolean?> = prefs.done.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val isDefaultHome = MutableStateFlow(homeRole.isHeld())
    private val error = MutableStateFlow<Int?>(null)
    private val requested = savedState.getStateFlow(STEP, OnboardingStep.Welcome)
    private val askedForAccess = savedState.getStateFlow(ASKED_FOR_ACCESS, false)

    private val facts: SetupFacts
        get() = SetupFacts(isDefaultHome.value, listenerAccess.granted.value)

    val ui: StateFlow<OnboardingUi> = combine(
        requested,
        isDefaultHome,
        listenerAccess.granted,
        askedForAccess,
        error,
    ) { requested, isHome, granted, asked, error ->
        val facts = SetupFacts(isHome, granted)
        val step = stepToShow(requested, facts)
        OnboardingUi(
            step = step,
            canGoBack = stepBefore(step, facts) != null,
            showRestrictedHelp = asked && !granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
            error = error,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, OnboardingUi(OnboardingStep.Welcome, false, false, null))

    /** The default-home choice sends no event; this runs on every start and after the system dialog. */
    fun recheck() {
        isDefaultHome.value = homeRole.isHeld()
    }

    fun next() {
        val after = stepAfter(ui.value.step, facts)
        if (after == null) finish() else goTo(after)
    }

    fun back() {
        stepBefore(ui.value.step, facts)?.let(::goTo)
    }

    fun homeRequestIntent(): Intent = homeRole.requestIntent()

    fun onHomeRequestMissing() {
        error.value = R.string.error_settings_missing
    }

    fun openAccessSettings() {
        savedState[ASKED_FOR_ACCESS] = true
        report(listenerAccess.openSettings())
    }

    fun openAppInfo() = report(actions.openOwnAppInfo())

    private fun finish() {
        viewModelScope.launch { prefs.markDone() }
    }

    private fun goTo(step: OnboardingStep) {
        error.value = null
        savedState[STEP] = step
    }

    private fun report(result: ActionResult) {
        error.value = (result as? ActionResult.Failed)?.message
    }

    private companion object {
        const val STEP = "onboarding_step"
        const val ASKED_FOR_ACCESS = "onboarding_asked_for_access"
    }
}
