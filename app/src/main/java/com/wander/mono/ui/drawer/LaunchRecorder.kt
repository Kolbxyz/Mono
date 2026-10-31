package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.LaunchHours
import com.wander.mono.di.AppScope
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Counts an app opened from the drawer (its list or search) in [LaunchHours], for [AppPrediction].
 * Never a private space app nor a hidden one: what the user keeps out of sight leaves no habit.
 */
class LaunchRecorder @Inject constructor(
    private val hours: LaunchHours,
    private val catalog: AppCatalog,
    private val prefs: LauncherPrefs,
    @AppScope private val scope: CoroutineScope,
) {
    fun opened(app: LaunchableApp) {
        if (app.isPrivate) return
        // Not loaded yet, it would forget every app: count nothing rather than wipe the habits.
        val installed = catalog.apps.value?.map { it.key }?.toSet() ?: return
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        scope.launch {
            if (app.key !in prefs.hidden.first()) hours.record(app.key, hour, installed)
        }
    }
}
