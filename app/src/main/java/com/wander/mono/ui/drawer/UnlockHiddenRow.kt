package com.wander.mono.ui.drawer

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R

/** Under locked search results: confirm the screen lock to search hidden apps too. */
@Composable
fun UnlockHiddenRow(onUnlock: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onUnlock, modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Icon(painterResource(R.drawable.sym_lock), contentDescription = null)
        Text(
            stringResource(R.string.drawer_unlock_hidden),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
