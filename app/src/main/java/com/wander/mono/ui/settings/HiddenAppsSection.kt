package com.wander.mono.ui.settings

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.ui.components.rememberCredentialPrompt

/**
 * Hidden apps: the lock (turning it off needs the screen lock too, or it would protect nothing)
 * and the list with Unhide, shown once unlocked. Unlocking lasts until the section leaves the screen.
 */
@Composable
@Suppress("UnusedParameter") // Every section takes the shared settings; this one keeps its own state.
fun HiddenAppsSection(settings: SettingsViewModel, hidden: HiddenAppsViewModel = viewModel()) {
    val apps by hidden.apps.collectAsStateWithLifecycle()
    val locks by hidden.locks.collectAsStateWithLifecycle()
    val revealed by hidden.revealed.collectAsStateWithLifecycle()
    var turnOffAfterUnlock by remember { mutableStateOf(false) }
    val prompt = rememberCredentialPrompt(
        R.string.hidden_unlock_title,
        onSuccess = {
            hidden.unlock()
            if (turnOffAfterUnlock) hidden.setLocks(false)
            turnOffAfterUnlock = false
        },
        onNoScreenLock = hidden::reportNoScreenLock,
    )
    DisposableEffect(hidden) { onDispose(hidden::relock) }
    SettingsGroup(R.string.settings_hidden) {
        locks?.let { on ->
            SwitchRow(
                checked = on,
                onCheckedChange = { lock ->
                    if (!lock && revealed == false) {
                        turnOffAfterUnlock = true
                        prompt()
                    } else {
                        hidden.setLocks(lock)
                    }
                },
                title = R.string.settings_hidden_lock,
                summary = R.string.settings_hidden_lock_summary,
            )
        }
        val list = apps
        when {
            revealed == false -> ActionRow(R.string.settings_hidden_show) {
                turnOffAfterUnlock = false
                prompt()
            }
            list == null -> Unit
            list.isEmpty() -> ListItem { Text(stringResource(R.string.settings_hidden_empty)) }
            else -> list.forEach { app ->
                ListItem(
                    trailingContent = {
                        TextButton(onClick = { hidden.unhide(app.key) }) {
                            Text(stringResource(R.string.sheet_unhide))
                        }
                    },
                ) {
                    Text(app.label)
                }
            }
        }
    }
}
