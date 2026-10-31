package com.wander.mono.ui.drawer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wander.mono.R
import com.wander.mono.ui.components.AppIcons

/** "Note “buy milk”", on one line: tapping hands the text to the app the user picks to keep it. */
@Composable
fun NoteRow(hit: SearchHit.Note, icons: AppIcons, onNote: () -> Unit, modifier: Modifier = Modifier) {
    SymbolRow(
        symbol = R.drawable.sym_edit,
        title = stringResource(R.string.command_note, hit.text),
        subtitle = null,
        clickLabel = R.string.command_note_action,
        icons = icons,
        onClick = onNote,
        modifier = modifier,
    )
}

/** "Call Anna" (or the number typed), on one line: tapping opens the dialer with the number in it. */
@Composable
fun DialRow(hit: SearchHit.Dial, icons: AppIcons, onDial: () -> Unit, modifier: Modifier = Modifier) {
    SymbolRow(
        symbol = R.drawable.sym_call,
        title = stringResource(R.string.command_call, hit.label),
        subtitle = null,
        clickLabel = R.string.search_reach_call,
        icons = icons,
        onClick = onDial,
        modifier = modifier,
    )
}
