package com.wander.mono.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import com.wander.mono.R
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.theme.secondaryTextColor

/**
 * "While you were away" on home's card look: the icons of the apps that posted during quiet mode
 * and how many notifications came in. Tapping it opens their latest notifications, each under its
 * app's icon, as preview rows (or, with previews off, dismisses it); the close button dismisses it.
 * Once home has shown it and stops (the user left, or the screen went off), [onSeen] clears it.
 */
@Composable
fun DigestCard(
    row: DigestRow,
    icons: AppIcons,
    previews: NotificationPreviewViewModel,
    onSeen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    var expanded by rememberSaveable { mutableStateOf(false) }
    LifecycleStartEffect(Unit) {
        // Leaving the composition while home is in view (scrolled away) is not having seen it go.
        onStopOrDispose { if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) onSeen() }
    }
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = HomeCardMargin),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClickLabel = stringResource(
                        if (row.previews.isEmpty()) R.string.digest_dismiss else R.string.digest_expand,
                    ),
                ) { if (row.previews.isEmpty()) onSeen() else expanded = !expanded }
                .padding(start = HomeCardInset, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.apps.take(MAX_ICONS).forEach { app -> AppIcon(app.key, icons, size = DIGEST_ICON) }
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.digest_title), style = MaterialTheme.typography.titleSmall)
                Text(
                    pluralStringResource(R.plurals.digest_count, row.count, row.count),
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryTextColor(),
                )
            }
            IconButton(onClick = onSeen) {
                Icon(
                    painterResource(R.drawable.sym_close),
                    contentDescription = stringResource(R.string.digest_dismiss),
                )
            }
        }
        AnimatedVisibility(
            visible = expanded && row.previews.isNotEmpty(),
            enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
            exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
        ) {
            Column(
                Modifier.padding(start = HomeCardInset, end = HomeCardInset, bottom = HomeCardInset),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.previews.forEach { preview ->
                    key(preview.key) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AppIcon(preview.app.key, icons, size = PREVIEW_ICON)
                            NotificationPreviewRow(preview, Modifier.weight(1f), previews)
                        }
                    }
                }
            }
        }
    }
}

/** How many icons the card shows; the count still adds up every app. */
private const val MAX_ICONS = 5
private val DIGEST_ICON = 28.dp
private val PREVIEW_ICON = 24.dp
