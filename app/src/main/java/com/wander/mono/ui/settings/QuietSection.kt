package com.wander.mono.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R

/**
 * Quiet mode: the switch, the favourites that stay on home, and (with Do Not Disturb access, which
 * the row asks for with its reason) Android's Do Not Disturb switched together with it.
 */
@Composable
@Suppress("UnusedParameter") // Every section takes the shared settings; this one keeps its own state.
fun QuietSection(settings: SettingsViewModel, quiet: QuietSettingsViewModel = viewModel()) {
    val enabled by quiet.enabled.collectAsStateWithLifecycle()
    val choices by quiet.choices.collectAsStateWithLifecycle()
    val links by quiet.linksDoNotDisturb.collectAsStateWithLifecycle()
    val granted by quiet.doNotDisturbGranted.collectAsStateWithLifecycle()
    LifecycleStartEffect(quiet) {
        quiet.recheckAccess()
        onStopOrDispose {}
    }
    SettingsGroup(R.string.settings_quiet) {
        enabled?.let { on ->
            SwitchRow(on, quiet::setEnabled, R.string.settings_quiet_mode, R.string.settings_quiet_mode_summary)
        }
        Text(
            stringResource(R.string.settings_quiet_apps),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        )
        choices?.let { list ->
            if (list.isEmpty()) {
                ListItem { Text(stringResource(R.string.settings_quiet_apps_empty)) }
            }
            list.forEach { choice ->
                ListItem(
                    leadingContent = { Checkbox(checked = choice.quiet, onCheckedChange = null) },
                    modifier = Modifier.toggleable(choice.quiet, role = Role.Checkbox) {
                        quiet.setQuiet(choice.app.key, it)
                    },
                ) {
                    Text(choice.app.label)
                }
            }
        }
        if (quiet.doNotDisturbSupported) {
            val link = links
            when {
                !granted -> ActionRow(
                    R.string.settings_quiet_dnd_grant,
                    R.string.settings_quiet_dnd_grant_summary,
                    quiet::openDoNotDisturbAccess,
                )
                link != null -> SwitchRow(
                    link,
                    quiet::setLinksDoNotDisturb,
                    R.string.settings_quiet_dnd,
                    R.string.settings_quiet_dnd_summary,
                )
            }
        }
    }
}
