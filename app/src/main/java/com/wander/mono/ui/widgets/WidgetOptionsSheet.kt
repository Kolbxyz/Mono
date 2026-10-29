package com.wander.mono.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.widgets.WidgetSize
import com.wander.mono.ui.components.ReadableLayout
import com.wander.mono.ui.components.SegmentedColumn
import com.wander.mono.ui.sheet.SheetAction

/** Long-press options for a widget row: its height, its own settings when it has them, its place, removal. */
@Composable
fun WidgetOptionsSheet(
    state: WidgetOptionsState,
    onDismiss: () -> Unit,
    onResize: (WidgetSize) -> Unit,
    onReconfigure: () -> Unit,
    onMove: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetMaxWidth = ReadableLayout.MaxColumnWidth,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(Modifier.padding(bottom = 24.dp)) {
            Text(
                state.widget.label,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            Text(
                stringResource(R.string.widget_size),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                WidgetSize.entries.forEachIndexed { index, size ->
                    SegmentedButton(
                        selected = size == state.size,
                        onClick = { onResize(size) },
                        shape = SegmentedButtonDefaults.itemShape(index, WidgetSize.entries.size),
                    ) {
                        Text(stringResource(size.label()))
                    }
                }
            }
            SegmentedColumn(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                if (state.widget.canReconfigure) {
                    SheetAction(stringResource(R.string.widget_reconfigure), R.drawable.sym_settings, onReconfigure)
                }
                if (state.canMoveUp) {
                    SheetAction(stringResource(R.string.sheet_move_up), R.drawable.sym_keyboard_arrow_up) { onMove(-1) }
                }
                if (state.canMoveDown) {
                    SheetAction(stringResource(R.string.sheet_move_down), R.drawable.sym_keyboard_arrow_down) {
                        onMove(1)
                    }
                }
                SheetAction(stringResource(R.string.widget_remove), R.drawable.sym_delete, onRemove)
            }
        }
    }
}

private fun WidgetSize.label(): Int = when (this) {
    WidgetSize.Small -> R.string.widget_size_small
    WidgetSize.Medium -> R.string.widget_size_medium
    WidgetSize.Large -> R.string.widget_size_large
}
