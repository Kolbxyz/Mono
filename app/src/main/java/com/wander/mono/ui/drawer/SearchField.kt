package com.wander.mono.ui.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import com.wander.mono.R
import com.wander.mono.data.RecentSearch

/**
 * The drawer's search box, with Mono's settings at its end. The keyboard's action key opens the top
 * result. [focusRequester] lets the drawer focus it when opened from home's search bar. While it is
 * focused and empty, the [recent] searches show under it as chips ([RecentSearchChips]).
 */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onOpenSettings: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    recent: List<RecentSearch> = emptyList(),
    onForgetRecent: (RecentSearch) -> Unit = {},
) {
    var focused by remember { mutableStateOf(false) }
    Column(modifier) {
        Field(query, onQueryChange, onSubmit, onOpenSettings, focusRequester) { focused = it }
        RecentSearchChips(focused && query.isEmpty(), recent, onUse = onQueryChange, onForget = onForgetRecent)
    }
}

@Composable
private fun Field(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onOpenSettings: () -> Unit,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChange(it.isFocused) },
        singleLine = true,
        shape = CircleShape,
        // One line, as the field is: at large font sizes the hint ends in an ellipsis instead of wrapping.
        placeholder = {
            Text(stringResource(R.string.drawer_search_hint), maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingIcon = { Icon(painterResource(R.drawable.sym_search), contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onOpenSettings) {
                Icon(painterResource(R.drawable.sym_settings), stringResource(R.string.settings_title))
            }
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Go,
        ),
        keyboardActions = KeyboardActions(onGo = { onSubmit() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}
