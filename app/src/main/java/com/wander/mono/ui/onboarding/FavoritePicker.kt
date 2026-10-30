package com.wander.mono.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons

/**
 * The favourites step: its title and why, then every app with its icon and a tick box, most used
 * first when Mono may read usage. A long list gets a filter field above it; ticks survive the
 * filter. Past [CALM_LIMIT] ticks a line says fewer is calmer; it never refuses.
 */
@Composable
internal fun FavoritePicker(picker: FavoritePickerViewModel, modifier: Modifier = Modifier) {
    val picks by picker.picks.collectAsStateWithLifecycle()
    val icons by picker.icons.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf("") }
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            stringResource(R.string.onboarding_favorites_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.semantics { heading() },
        )
        val current = picks
        val tooMany = current != null && current.picked.size > CALM_LIMIT
        Text(
            stringResource(if (tooMany) R.string.onboarding_favorites_calm else R.string.onboarding_favorites_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // Nothing until the apps are loaded: an empty list would read as "no apps".
        if (current != null) {
            if (current.apps.size > FILTER_FROM) FilterField(filter, onChange = { filter = it })
            val shown = remember(current.apps, filter) {
                val query = filter.trim()
                current.apps.filter { query.isEmpty() || it.label.contains(query, ignoreCase = true) }
            }
            if (shown.isEmpty()) {
                Text(
                    stringResource(R.string.drawer_no_results),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(shown, key = { it.key.encode() }) { app ->
                    PickRow(app, icons, checked = app.key in current.picked, onToggle = { picker.toggle(app.key) })
                }
            }
        }
    }
}

/** Narrows the list by name; the keyboard's action only puts the keyboard away. */
@Composable
private fun FilterField(text: String, onChange: (String) -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = text,
        onValueChange = onChange,
        placeholder = { Text(stringResource(R.string.onboarding_favorites_filter)) },
        leadingIcon = { Icon(painterResource(R.drawable.sym_search), contentDescription = null) },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        modifier = Modifier.fillMaxWidth(),
    )
}

/** One app: its icon, its name (and profile), a tick box; the whole row ticks. */
@Composable
private fun PickRow(app: LaunchableApp, icons: AppIcons, checked: Boolean, onToggle: () -> Unit) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { AppIcon(app.key, icons) },
        supportingContent = app.detail?.let { { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
        trailingContent = { Checkbox(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(checked, role = Role.Checkbox) { onToggle() },
    ) {
        Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** More favourites than this and the step suggests fewer (spec 7.1). */
private const val CALM_LIMIT = 9

/** Longer lists than this get a filter field: more than a screenful to scroll through. */
private const val FILTER_FROM = 12
