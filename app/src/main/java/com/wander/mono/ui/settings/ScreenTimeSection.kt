package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.ScreenTimeSettings

/**
 * Opt-in screen time, reading usage access: today's time in an app's long-press options, and the
 * nudge that fades a favourite past the daily limit set there. On only while the access is granted;
 * turning it on without it opens Android's page to grant it, and until then the row says so.
 */
@Composable
fun ScreenTimeSection(settings: SettingsViewModel, access: UsageAccessViewModel = viewModel()) {
    val granted by access.granted.collectAsStateWithLifecycle()
    LifecycleStartEffect(access) {
        access.recheck()
        onStopOrDispose {}
    }
    val on = settings.collect(ScreenTimeSettings.Nudge) ?: return
    SettingsGroup(R.string.settings_screen_time) {
        SwitchRow(
            checked = on && granted,
            onCheckedChange = { checked ->
                settings.set(ScreenTimeSettings.Nudge, checked)
                if (checked && !granted) access.openSettings()
            },
            title = R.string.settings_screen_time_nudge,
            summary = if (on && !granted) R.string.settings_usage_grant_short else R.string.settings_screen_time_short,
            icon = R.drawable.sym_hourglass_bottom,
        )
    }
}
