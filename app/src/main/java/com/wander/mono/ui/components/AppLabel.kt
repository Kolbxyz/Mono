package com.wander.mono.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp

/**
 * An app's name, its work-profile badge and its notification dot, shared by home and drawer rows.
 * When another app has the same name, the [LaunchableApp.detail] that tells them apart goes underneath.
 */
@Composable
fun AppLabel(
    app: LaunchableApp,
    hasNotifications: Boolean,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    val notificationsState = stringResource(R.string.state_has_notifications)
    Column(modifier.semantics { if (hasNotifications) stateDescription = notificationsState }) {
        NameRow(app, hasNotifications, style)
        app.detail?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun NameRow(app: LaunchableApp, hasNotifications: Boolean, style: TextStyle) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            app.label,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (!app.isMainProfile) {
            Icon(
                painterResource(R.drawable.sym_work),
                contentDescription = stringResource(R.string.state_work_profile),
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp),
            )
        }
        if (hasNotifications) NotificationDot()
    }
}
