package com.wander.mono.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.quiet.QuietMode
import com.wander.mono.core.quiet.QuietZenRule
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.QuietApps
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A favourite and whether it stays on home in quiet mode. */
data class QuietChoice(val app: LaunchableApp, val quiet: Boolean)

/** The quiet mode settings: the switch, which favourites stay, and the link to Do Not Disturb. */
@HiltViewModel
class QuietSettingsViewModel @Inject constructor(
    catalog: AppCatalog,
    prefs: LauncherPrefs,
    private val quietApps: QuietApps,
    private val quiet: QuietMode,
    private val zenRule: QuietZenRule,
    private val messages: UserMessages,
) : ViewModel() {
    val enabled: StateFlow<Boolean?> = quiet.enabled.stateIn(viewModelScope, STARTED, null)

    val linksDoNotDisturb: StateFlow<Boolean?> = quiet.linksDoNotDisturb.stateIn(viewModelScope, STARTED, null)

    /** The favourites in home order; null until the catalog is loaded. */
    val choices: StateFlow<List<QuietChoice>?> = combine(
        prefs.favorites,
        catalog.apps.filterNotNull(),
        quietApps.apps,
    ) { favorites, apps, quietKeys ->
        val installed = apps.associateBy { it.key }
        favorites.mapNotNull(installed::get).map { QuietChoice(it, it.key in quietKeys) }
    }.stateIn(viewModelScope, STARTED, null)

    val doNotDisturbSupported: Boolean get() = zenRule.isSupported

    private val mutableDndGranted = MutableStateFlow(zenRule.isGranted())

    /** Granted in Android's settings, which sends no event: re-read when settings come back. */
    val doNotDisturbGranted: StateFlow<Boolean> = mutableDndGranted.asStateFlow()

    fun recheckAccess() {
        mutableDndGranted.value = zenRule.isGranted()
    }

    fun setEnabled(on: Boolean) {
        viewModelScope.launch { quiet.set(on) }
    }

    fun setQuiet(key: AppKey, on: Boolean) {
        viewModelScope.launch { quietApps.setQuiet(key, on) }
    }

    fun setLinksDoNotDisturb(link: Boolean) {
        viewModelScope.launch { quiet.setLinksDoNotDisturb(link) }
    }

    fun openDoNotDisturbAccess() {
        messages.report(zenRule.openAccessSettings())
    }

    private companion object {
        val STARTED = SharingStarted.WhileSubscribed(5_000L)
    }
}
