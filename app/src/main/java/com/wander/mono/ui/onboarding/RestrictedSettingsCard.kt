package com.wander.mono.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R

/**
 * Android 13+ greys out the notification-access switch for apps installed outside a store session
 * (GrapheneOS does the same) until the user allows restricted settings from the app's info page.
 */
@Composable
internal fun RestrictedSettingsCard(onOpenAppInfo: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.onboarding_restricted_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.onboarding_restricted_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            listOf(
                R.string.onboarding_restricted_step1,
                R.string.onboarding_restricted_step2,
                R.string.onboarding_restricted_step3,
            ).forEach { Text(stringResource(it), style = MaterialTheme.typography.bodyMedium) }
            FilledTonalButton(onClick = onOpenAppInfo) {
                Text(stringResource(R.string.onboarding_restricted_action))
            }
        }
    }
}
