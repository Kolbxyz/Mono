package com.wander.mono.ui.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp

/** The rename dialog of [rename], while it has an app to rename. */
@Composable
fun RenameDialog(rename: RenameViewModel) {
    val app by rename.app.collectAsStateWithLifecycle()
    app?.let { target ->
        RenameDialog(target, onRename = { rename.rename(target.key, it) }, onDismiss = rename::dismiss)
    }
}

/**
 * Renames [app] on home, in the drawer and in search. Starts on the current name, all selected;
 * saving a blank name, or "Use original name", goes back to the app's own.
 */
@Composable
private fun RenameDialog(app: LaunchableApp, onRename: (String?) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable(app.key, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(app.label, TextRange(0, app.label.length)))
    }
    val focus = remember { FocusRequester() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_title)) },
        text = {
            // The dialog is its own window: focus and the keyboard are asked for from inside it.
            val keyboard = LocalSoftwareKeyboardController.current
            LaunchedEffect(focus) {
                focus.requestFocus()
                keyboard?.show()
            }
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.rename_field)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onRename(text.text) }),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus),
                )
                app.systemLabel?.let { original ->
                    Text(
                        stringResource(R.string.rename_original, original),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    TextButton(onClick = { onRename(null) }) { Text(stringResource(R.string.rename_reset)) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onRename(text.text) }) { Text(stringResource(R.string.rename_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.rename_cancel)) } },
    )
}
