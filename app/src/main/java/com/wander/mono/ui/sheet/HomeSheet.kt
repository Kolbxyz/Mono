package com.wander.mono.ui.sheet

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.components.ActionButtonGroup
import com.wander.mono.ui.components.ActionEmphasis
import com.wander.mono.ui.components.MenuAction
import com.wander.mono.ui.components.MonoSheet

/**
 * Long-press options for home itself (the clock or an empty spot), as opposed to one app's: edit
 * home and add a widget first, then quiet mode (an on/off button) and settings.
 */
@Composable
fun HomeSheet(
    quiet: Boolean,
    onDismiss: () -> Unit,
    onSetQuiet: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onAddWidget: () -> Unit,
    onEditHome: () -> Unit,
) {
    val quietLabel = if (quiet) R.string.home_sheet_quiet_off else R.string.home_sheet_quiet_on
    val actions = listOf(
        MenuAction(R.drawable.sym_edit, R.string.home_sheet_edit, ActionEmphasis.Primary, onClick = onEditHome),
        MenuAction(
            R.drawable.sym_widgets,
            R.string.home_sheet_add_widget,
            ActionEmphasis.Secondary,
            onClick = onAddWidget,
        ),
        MenuAction(R.drawable.sym_quiet, quietLabel, selected = quiet) { onSetQuiet(!quiet) },
        MenuAction(R.drawable.sym_settings, R.string.home_sheet_settings, onClick = onOpenSettings),
    )
    MonoSheet(onDismissRequest = onDismiss) {
        ActionButtonGroup(actions, Modifier.padding(top = 8.dp, bottom = 24.dp))
    }
}
