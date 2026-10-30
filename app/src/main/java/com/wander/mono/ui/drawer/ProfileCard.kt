package com.wander.mono.ui.drawer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.home.onWallpaper
import com.wander.mono.ui.theme.secondaryTextColor

/**
 * A profile under the alphabetical list (the private space, a work profile), straight on the
 * drawer's background like the letter groups: a header saying what state it is in with its buttons,
 * its apps under that while it is [open]. The apps fold away when it locks and unfold when it opens.
 */
@Composable
internal fun ProfileCard(
    @StringRes title: Int,
    @DrawableRes symbol: Int,
    @StringRes status: Int,
    open: Boolean,
    apps: List<DrawerEntry>,
    icons: AppIcons,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    // The apps from when it was last open, kept while they fold away so locking animates them out.
    val lastOpen = remember { LastOpen(openAtStart = open) }
    if (open) lastOpen.apps = apps
    Column(modifier) {
        Text(
            stringResource(title),
            // As the list's word headings ("New"), so the profiles read as more of the same list.
            style = MaterialTheme.typography.titleSmall.onWallpaper(),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                .semantics { heading() },
        )
        ProfileHeader(symbol, status, actions)
        val labelStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium).onWallpaper()
        lastOpen.apps.forEach { entry ->
            key(entry.app.key) {
                // Shown at once when the drawer opens on an open profile; unfolds when it opens here.
                val visible = remember { MutableTransitionState(lastOpen.openAtStart) }
                visible.targetState = open
                AnimatedVisibility(
                    visibleState = visible,
                    enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                    exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
                ) {
                    DrawerAppRow(
                        entry = entry,
                        icons = icons,
                        onLaunch = { onLaunchApp(entry.app) },
                        onLongPress = { onLongPress(entry.app) },
                        labelStyle = labelStyle,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(@DrawableRes symbol: Int, @StringRes status: Int, actions: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(symbol), null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Text(
            stringResource(status),
            style = MaterialTheme.typography.bodyMedium.onWallpaper(),
            color = secondaryTextColor(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

/** Not snapshot state: only read while composing the section, which opening or locking recomposes anyway. */
private class LastOpen(val openAtStart: Boolean) {
    var apps: List<DrawerEntry> = emptyList()
}
