package com.wander.mono.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.wander.mono.R

/**
 * The inline reply: focused as soon as it opens, and kept above the keyboard as it slides in (the
 * home list pads the IME through `safeDrawing`; this scrolls the field into the freed space).
 * [onSend] returns true once sent, which clears the text.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReplyField(hint: String?, onSend: (String) -> Boolean, modifier: Modifier = Modifier) {
    var text by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val bringIntoView = remember { BringIntoViewRequester() }
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(Unit) { focus.requestFocus() }
    LaunchedEffect(imeBottom) { if (imeBottom > 0) bringIntoView.bringIntoView() }

    val send = {
        if (text.isNotBlank() && onSend(text.trim())) text = ""
    }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text(hint ?: stringResource(R.string.preview_reply_hint)) },
        shape = MaterialTheme.shapes.extraLarge,
        maxLines = MAX_LINES,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Send,
        ),
        keyboardActions = KeyboardActions(onSend = { send() }),
        trailingIcon = {
            FilledTonalIconButton(onClick = send, enabled = text.isNotBlank()) {
                Icon(painterResource(R.drawable.sym_send), contentDescription = stringResource(R.string.preview_send))
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focus)
            .bringIntoViewRequester(bringIntoView),
    )
}

/** A reply is a line or two; past that the field scrolls rather than pushing the list away. */
private const val MAX_LINES = 4
