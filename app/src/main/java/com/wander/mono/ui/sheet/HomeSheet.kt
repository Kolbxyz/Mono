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
fun HomeSheet(onDismiss: () -> Unit, onOpenSettings: () -> Unit, onEditHome: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 16.dp)) {
            SheetAction(stringResource(R.string.home_sheet_edit), onEditHome)
            SheetAction(stringResource(R.string.home_sheet_settings), onOpenSettings)
        }
    }
}
