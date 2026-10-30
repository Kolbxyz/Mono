package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.ScreenTimeSettings

/**
 * Opt-in screen-time nudge: a favourite past the daily limit set in its options fades on home.
 * Like today's time in the sheet, it needs usage access: turning it on without it opens Android's
 * page to grant it, and until then the row says so.
 */
@Composable
fun ScreenTimeSection(settings: SettingsViewModel, access: UsageAccessViewModel = viewModel()) {
    val nudge = settings.collect(ScreenTimeSettings.Nudge) ?: return
    val granted by access.granted.collectAsStateWithLifecycle()
    LifecycleStartEffect(access) {
        access.recheck()
        onStopOrDispose {}
    }
    SettingsGroup(R.string.settings_screen_time) {
        SwitchRow(
            checked = nudge && granted,
            onCheckedChange = { on ->
                settings.set(ScreenTimeSettings.Nudge, on)
                if (on && !granted) access.openSettings()
            },
            title = R.string.settings_screen_time_nudge,
            summary = if (nudge && !granted) {
                R.string.settings_usage_grant_short
            } else {
                R.string.settings_screen_time_short
            },
            icon = R.drawable.sym_hourglass_bottom,
        )
    }
}
