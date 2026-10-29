package com.wander.mono.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** The small dot next to an app with notifications. Its meaning is announced by the row, not by the dot. */
@Composable
fun NotificationDot(modifier: Modifier = Modifier) {
    Box(modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
}
