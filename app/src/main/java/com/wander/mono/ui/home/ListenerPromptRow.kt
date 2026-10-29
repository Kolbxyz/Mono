package com.wander.mono.ui.home

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wander.mono.R

/** Shown instead of dots and media when notification access is missing; opens the system page for it. */
@Composable
fun ListenerPromptRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(stringResource(R.string.home_listener_prompt))
    }
}
