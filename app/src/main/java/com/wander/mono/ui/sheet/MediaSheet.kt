package com.wander.mono.ui.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.ui.components.MonoSheet
import com.wander.mono.ui.settings.MediaSection
import com.wander.mono.ui.settings.SettingsViewModel

/**
 * The player's sheet, from a long press on it under the clock: whether it shows, how it opens,
 * whether a swipe skips and how long it stays once paused.
 * The player under the sheet follows each change.
 */
@Composable
fun MediaSheet(onDismiss: () -> Unit, settings: SettingsViewModel = viewModel()) {
    MonoSheet(onDismissRequest = onDismiss) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            MediaSection(settings)
        }
    }
}
