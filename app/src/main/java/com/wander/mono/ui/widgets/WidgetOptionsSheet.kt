package com.wander.mono.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.widgets.WidgetSize
import com.wander.mono.ui.components.ActionButtonGroup
import com.wander.mono.ui.components.ActionEmphasis
import com.wander.mono.ui.components.MenuAction
import com.wander.mono.ui.components.MonoSheet
import com.wander.mono.ui.sheet.SheetHeader

/**
 * Long-press options for a widget row: its name, its height as a connected button group, then
 * its own settings when it has them, its place and removal as one button group.
 */
@Composable
fun WidgetOptionsSheet(
    state: WidgetOptionsState,
    onDismiss: () -> Unit,
    onResize: (WidgetSize) -> Unit,
    onReconfigure: () -> Unit,
    onMove: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    val actions = buildList {
        if (state.widget.canReconfigure) {
            add(MenuAction(R.drawable.sym_settings, R.string.widget_reconfigure, onClick = onReconfigure))
        }
        if (state.canMoveUp) {
            add(MenuAction(R.drawable.sym_keyboard_arrow_up, R.string.sheet_move_up, ActionEmphasis.Icon) {
                onMove(-1)
            })
        }
        if (state.canMoveDown) {
            add(MenuAction(R.drawable.sym_keyboard_arrow_down, R.string.sheet_move_down, ActionEmphasis.Icon) {
                onMove(1)
            })
        }
        add(MenuAction(R.drawable.sym_delete, R.string.widget_remove, ActionEmphasis.Danger, onClick = onRemove))
    }
    MonoSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 24.dp)) {
            SheetHeader(state.widget.label)
            Text(
                stringResource(R.string.widget_size),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            WidgetSizeButtons(state.size, onResize, Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            ActionButtonGroup(actions, Modifier.padding(top = 16.dp))
        }
    }
}
