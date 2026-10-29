package com.wander.mono.ui.quiet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R

/**
 * In quiet mode the drawer opens on a pause instead of the app list: all apps show only after an
 * explicit tap. The choice lives as long as this composable, so each opening of the drawer asks again.
 */
@Composable
fun QuietDrawerGate(quiet: Boolean, onClose: () -> Unit, drawer: @Composable () -> Unit) {
    var showAll by rememberSaveable { mutableStateOf(false) }
    if (!quiet || showAll) {
        drawer()
        return
    }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.Start,
        ) {
            Icon(
                painterResource(R.drawable.sym_quiet),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Text(stringResource(R.string.quiet_gate_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(R.string.quiet_gate_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = { showAll = true }) { Text(stringResource(R.string.quiet_gate_show_all)) }
            TextButton(onClick = onClose) { Text(stringResource(R.string.quiet_gate_back)) }
        }
    }
}
