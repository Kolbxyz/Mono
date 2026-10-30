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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.theme.secondaryTextColor

/**
 * An app's name, its work-profile badge and its notification badge, shared by home and drawer rows.
 * When another app has the same name, the [LaunchableApp.detail] that tells them apart goes underneath.
 * [name] is the app's label as drawn, styled in parts where search highlights what matched.
 */
@Composable
fun AppLabel(
    app: LaunchableApp,
    badge: AppBadge,
    style: TextStyle,
    modifier: Modifier = Modifier,
    name: AnnotatedString = AnnotatedString(app.label),
) {
    val badgeState = when (badge) {
        AppBadge.None -> null
        AppBadge.Dot -> stringResource(R.string.state_has_notifications)
        is AppBadge.Count -> pluralStringResource(R.plurals.state_notification_count, badge.count, badge.count)
    }
    Column(modifier.semantics { badgeState?.let { stateDescription = it } }) {
        NameRow(app, name, badge, style)
        app.detail?.let {
            Text(
                it,
                // The name's shadow too, when it is drawn on the wallpaper.
                style = MaterialTheme.typography.bodySmall.copy(shadow = style.shadow),
                color = secondaryTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun NameRow(app: LaunchableApp, name: AnnotatedString, badge: AppBadge, style: TextStyle) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            name,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (!app.isMainProfile) {
            Icon(
                painterResource(if (app.isPrivate) R.drawable.sym_lock else R.drawable.sym_work),
                contentDescription = stringResource(
                    if (app.isPrivate) R.string.state_private_space else R.string.state_work_profile,
                ),
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp),
            )
        }
        when (badge) {
            AppBadge.None -> Unit
            AppBadge.Dot -> NotificationDot()
            is AppBadge.Count -> CountBadge(badge)
        }
    }
}
