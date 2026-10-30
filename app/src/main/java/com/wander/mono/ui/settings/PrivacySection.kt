package com.wander.mono.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
 * "What Mono can see": first that Mono has no internet permission, then every access it may hold,
 * what it is used for and whether it is allowed now. Read-only: a tap opens Android's own page for
 * that access, where the user changes it.
 */
@Composable
fun PrivacySection(privacy: PrivacyViewModel = viewModel()) {
    val accesses by privacy.accesses.collectAsStateWithLifecycle()
    LifecycleStartEffect(privacy) {
        privacy.recheck()
        onStopOrDispose {}
    }
    SettingsGroup(R.string.privacy_title) {
        ListItem(
            colors = settingRowColors(),
            leadingContent = leadingBadge(R.drawable.sym_shield),
            supportingContent = { Text(stringResource(R.string.privacy_no_internet_body)) },
        ) {
            Text(stringResource(R.string.privacy_no_internet))
        }
        accesses.forEach { AccessRow(it, onOpen = { privacy.open(it.access) }) }
    }
}

@Composable
private fun AccessRow(state: AccessState, onOpen: () -> Unit) {
    val access = state.access
    val scheme = MaterialTheme.colorScheme
    ListItem(
        colors = settingRowColors(),
        leadingContent = leadingBadge(access.icon, enabled = state.granted),
        supportingContent = {
            Column {
                Text(stringResource(access.use))
                Text(
                    stringResource(if (state.granted) R.string.privacy_state_allowed else R.string.privacy_state_off),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (state.granted) scheme.primary else scheme.onSurfaceVariant,
                )
            }
        },
        modifier = Modifier.clickable(
            onClickLabel = stringResource(R.string.privacy_open_settings),
            role = Role.Button,
            onClick = onOpen,
        ),
    ) {
        Text(stringResource(access.title))
    }
}
