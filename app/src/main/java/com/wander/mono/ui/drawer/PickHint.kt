package com.wander.mono.ui.drawer

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.home.onWallpaper

/**
 * What the drawer is picking an app for ([hint]), over its list; with [onUseSearch], picking for
 * home's button once it opens an app, a way back to search instead of any app.
 */
@Composable
internal fun PickHint(@StringRes hint: Int, onUseSearch: (() -> Unit)?) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(hint),
            style = MaterialTheme.typography.titleMedium.onWallpaper(),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        if (onUseSearch != null) {
            TextButton(onClick = onUseSearch) { Text(stringResource(R.string.home_button_use_search)) }
        }
    }
}
