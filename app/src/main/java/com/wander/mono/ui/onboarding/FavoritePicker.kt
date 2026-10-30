package com.wander.mono.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wander.mono.R

/**
 * The favourites step: its title and why, then every app with a tick box, most used first when
 * Mono may read usage. Past [CALM_LIMIT] ticks a line says fewer is calmer; it never refuses.
 */
@Composable
internal fun FavoritePicker(picker: FavoritePickerViewModel, modifier: Modifier = Modifier) {
    val picks by picker.picks.collectAsStateWithLifecycle()
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
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(current.apps, key = { it.key.encode() }) { app ->
                    val checked = app.key in current.picked
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Checkbox(checked = checked, onCheckedChange = null) },
                        supportingContent = app.detail?.let { { Text(it) } },
                        modifier = Modifier.toggleable(checked, role = Role.Checkbox) { picker.toggle(app.key) },
                    ) {
                        Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

/** More favourites than this and the step suggests fewer (spec 7.1). */
private const val CALM_LIMIT = 9
