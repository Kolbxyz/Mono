package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.UsageSettings

/**
 * Opt-in screen time in an app's long-press options. Turning it on without usage access opens
 * Android's page to grant it; until granted the row says so and the sheet shows nothing.
 */
@Composable
fun UsageSection(settings: SettingsViewModel, access: UsageAccessViewModel = viewModel()) {
    val shown = settings.collect(UsageSettings.ShowInSheet) ?: return
    val granted by access.granted.collectAsStateWithLifecycle()
    LifecycleStartEffect(access) {
        access.recheck()
        onStopOrDispose {}
    }
    SettingsGroup(R.string.settings_usage) {
        SwitchRow(
            checked = shown && granted,
            onCheckedChange = { on ->
                settings.set(UsageSettings.ShowInSheet, on)
                if (on && !granted) access.openSettings()
            },
            title = R.string.settings_usage_in_sheet,
            summary = if (shown && !granted) R.string.settings_usage_grant_short else R.string.settings_usage_short,
            icon = R.drawable.sym_hourglass_bottom,
        )
    }
}
