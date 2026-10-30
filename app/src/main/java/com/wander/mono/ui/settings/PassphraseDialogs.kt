package com.wander.mono.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.components.MonoLoadingIndicator

/**
 * Before saving: seal the backup with a passphrase, typed twice, or save it plain. The fields are
 * never saved state: a passphrase is handed over as a character array and the fields cleared.
 */
@Composable
internal fun SealBackupDialog(onSeal: (CharArray) -> Unit, onPlain: () -> Unit, onDismiss: () -> Unit) {
    val first = remember { TextFieldState() }
    val second = remember { TextFieldState() }
    ClearOnLeave(first, second)
    val long = first.text.length >= MIN_PASSPHRASE
    val same = first.text.toString() == second.text.toString()
    val seal = { if (long && same) onSeal(first.take().also { second.clearText() }) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(painterResource(R.drawable.sym_lock), null) },
        title = { Text(stringResource(R.string.backup_seal_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.backup_seal_text))
                PassphraseField(
                    first,
                    R.string.backup_seal_passphrase,
                    isError = first.text.isNotEmpty() && !long,
                    supporting = pluralStringResource(R.plurals.backup_seal_short, MIN_PASSPHRASE, MIN_PASSPHRASE),
                )
                val mismatch = second.text.isNotEmpty() && !same
                PassphraseField(
                    second,
                    R.string.backup_seal_repeat,
                    isError = mismatch,
                    supporting = stringResource(R.string.backup_seal_mismatch).takeIf { mismatch },
                    onDone = seal,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = seal, enabled = long && same) {
                Text(stringResource(R.string.backup_seal_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onPlain) { Text(stringResource(R.string.backup_seal_skip)) } },
    )
}

/** A sealed backup's passphrase. Says so when the last one was wrong; waits while one is tried. */
@Composable
internal fun OpenSealedDialog(prompt: SealedPrompt, onOpen: (CharArray) -> Unit, onDismiss: () -> Unit) {
    val field = remember { TextFieldState() }
    ClearOnLeave(field)
    val open = { if (field.text.isNotEmpty() && !prompt.opening) onOpen(field.take()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(painterResource(R.drawable.sym_lock), null) },
        title = { Text(stringResource(R.string.backup_open_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.backup_open_text))
                PassphraseField(
                    field,
                    R.string.backup_seal_passphrase,
                    isError = prompt.wrong,
                    supporting = stringResource(R.string.backup_error_passphrase).takeIf { prompt.wrong },
                    enabled = !prompt.opening,
                    onDone = open,
                )
            }
        },
        confirmButton = {
            if (prompt.opening) {
                MonoLoadingIndicator()
            } else {
                TextButton(onClick = open, enabled = field.text.isNotEmpty()) {
                    Text(stringResource(R.string.backup_open_confirm))
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.backup_restore_cancel)) } },
    )
}

/**
 * A hidden-text field with an eye to show it while typing a long passphrase. The keyboard's action
 * moves to the next field, or, given [onDone], is Done and runs it.
 */
@Composable
private fun PassphraseField(
    state: TextFieldState,
    @StringRes label: Int,
    isError: Boolean,
    supporting: String?,
    enabled: Boolean = true,
    onDone: (() -> Unit)? = null,
) {
    var shown by remember { mutableStateOf(false) }
    OutlinedSecureTextField(
        state = state,
        label = { Text(stringResource(label)) },
        isError = isError,
        enabled = enabled,
        supportingText = if (supporting != null) {
            { Text(supporting) }
        } else {
            null
        },
        textObfuscationMode = if (shown) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
        trailingIcon = {
            IconButton(onClick = { shown = !shown }) {
                Icon(
                    painterResource(R.drawable.sym_visibility),
                    stringResource(if (shown) R.string.passphrase_hide else R.string.passphrase_show),
                    tint = if (shown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = if (onDone != null) ImeAction.Done else ImeAction.Next,
        ),
        onKeyboardAction = onDone?.let { done -> KeyboardActionHandler { done() } },
        modifier = Modifier.fillMaxWidth(),
    )
}

/** The field's text as a character array for the cipher, the field then emptied. */
private fun TextFieldState.take(): CharArray {
    val chars = CharArray(text.length) { text[it] }
    clearText()
    return chars
}

/** Clears the fields when the dialog goes, however it goes. */
@Composable
private fun ClearOnLeave(vararg fields: TextFieldState) {
    DisposableEffect(Unit) { onDispose { fields.forEach { it.clearText() } } }
}

/** Short enough to type on a phone, long enough that 310,000 rounds of PBKDF2 mean something. */
private const val MIN_PASSPHRASE = 8
