package com.wander.mono.ui.settings

import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R

/**
 * Quiet mode: the switch and (with Do Not Disturb access, which the row asks for with its reason)
 * Android's Do Not Disturb switched together with it, then the favourites that stay on home.
 */
@Composable
fun QuietSection(quiet: QuietSettingsViewModel = viewModel()) {
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
            SwitchRow(
                checked = on,
                onCheckedChange = quiet::setEnabled,
                title = R.string.settings_quiet_mode,
                summary = R.string.settings_quiet_mode_short,
                icon = R.drawable.sym_quiet,
            )
        }
        if (quiet.doNotDisturbSupported) {
            val link = links
            when {
                !granted -> ActionRow(
                    title = R.string.settings_quiet_dnd_grant,
                    summary = R.string.settings_quiet_dnd_grant_short,
                    icon = R.drawable.sym_do_not_disturb_on,
                    onClick = quiet::openDoNotDisturbAccess,
                )
                link != null -> SwitchRow(
                    checked = link,
                    onCheckedChange = quiet::setLinksDoNotDisturb,
                    title = R.string.settings_quiet_dnd,
                    icon = R.drawable.sym_do_not_disturb_on,
                )
            }
        }
    }
    // A group of its own, so its title is a real header, not a bare line drawn as a strip.
    SettingsGroup(R.string.settings_quiet_apps) {
        choices?.let { list ->
            if (list.isEmpty()) {
                ListItem(colors = settingRowColors()) { Text(stringResource(R.string.settings_quiet_apps_empty)) }
            }
            list.forEach { choice ->
                ListItem(
                    colors = settingRowColors(),
                    leadingContent = { Checkbox(checked = choice.quiet, onCheckedChange = null) },
                    modifier = Modifier.toggleable(choice.quiet, role = Role.Checkbox) {
                        quiet.setQuiet(choice.app.key, it)
                    },
                ) {
                    Text(choice.app.label)
                }
            }
        }
    }
}
