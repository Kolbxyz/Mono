package com.wander.mono.ui.home.folders

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import com.wander.mono.R
import com.wander.mono.data.cleanFolderName

/**
 * Names a folder, as the rename dialog names an app: a new one starts empty, a renamed one on its
 * current name, all selected. A name with nothing but spaces cannot be saved.
 */
@Composable
internal fun FolderNameDialog(naming: FolderNaming, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    val current = (naming as? FolderNaming.Rename)?.folder?.name.orEmpty()
    var text by rememberSaveable(naming, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(current, TextRange(0, current.length)))
    }
    val valid = cleanFolderName(text.text).isNotEmpty()
    val focus = remember { FocusRequester() }
    val isNew = naming is FolderNaming.New
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (isNew) R.string.folder_new_title else R.string.folder_rename_title)) },
        text = {
            // The dialog is its own window: focus and the keyboard are asked for from inside it.
            val keyboard = LocalSoftwareKeyboardController.current
            LaunchedEffect(focus) {
                focus.requestFocus()
                keyboard?.show()
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.folder_name_field)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { if (valid) onSave(text.text) }),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text.text) }, enabled = valid) {
                Text(stringResource(if (isNew) R.string.folder_create else R.string.rename_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.rename_cancel)) } },
    )
}
