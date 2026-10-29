package com.wander.mono.ui.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R

/** Long-press options for home itself (the clock or an empty spot), as opposed to one app's. */
@Composable
fun HomeSheet(
    quiet: Boolean,
    onDismiss: () -> Unit,
    onSetQuiet: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onAddWidget: () -> Unit,
    onEditHome: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 16.dp)) {
            SheetAction(stringResource(R.string.home_sheet_edit), onEditHome)
            val quietLabel = if (quiet) R.string.home_sheet_quiet_off else R.string.home_sheet_quiet_on
            SheetAction(stringResource(quietLabel)) { onSetQuiet(!quiet) }
            SheetAction(stringResource(R.string.home_sheet_add_widget), onAddWidget)
            SheetAction(stringResource(R.string.home_sheet_settings), onOpenSettings)
        }
    }
}
