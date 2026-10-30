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
import com.wander.mono.ui.settings.ClockPreviewSection
import com.wander.mono.ui.settings.ClockSection
import com.wander.mono.ui.settings.SettingsViewModel

/**
 * The clock's own sheet, from a long press on it: the live clock on top and, under it, the same
 * rows as in settings (font, weight, size, style, alignment, date), so each change shows as it is
 * made without leaving home.
 */
@Composable
fun ClockSheet(onDismiss: () -> Unit, settings: SettingsViewModel = viewModel()) {
    MonoSheet(onDismissRequest = onDismiss) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            ClockPreviewSection()
            ClockSection(settings)
        }
    }
}
