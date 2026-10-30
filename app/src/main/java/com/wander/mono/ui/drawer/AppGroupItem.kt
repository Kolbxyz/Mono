package com.wander.mono.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.SegmentedColumn

/** One group of the drawer's list: its heading, then its apps as one segmented card. */
@Composable
internal fun AppGroupItem(
    group: AppGroup,
    icons: AppIcons,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        GroupHeading(group.head)
        SegmentedColumn {
            group.entries.forEach { entry ->
                DrawerAppRow(
                    entry = entry,
                    icons = icons,
                    onLaunch = { onLaunchApp(entry.app) },
                    onLongPress = { onLongPress(entry.app) },
                )
            }
        }
    }
}

/** A letter, "New" (led by the same tertiary dot its apps carry) or how long ago, in primary. */
@Composable
private fun GroupHeading(head: GroupHead) {
    Row(
        Modifier
            .padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            .semantics(mergeDescendants = true) { heading() },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (head == GroupHead.New) {
            Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape))
        }
        Text(
            headingText(head),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun headingText(head: GroupHead): String = when (head) {
    is GroupHead.Letter -> head.letter
    GroupHead.New -> stringResource(R.string.drawer_group_new)
    is GroupHead.Age -> stringResource(
        when (head.age) {
            InstallAge.Day -> R.string.drawer_group_day
            InstallAge.Week -> R.string.drawer_group_week
            InstallAge.Month -> R.string.drawer_group_month
            InstallAge.Earlier -> R.string.drawer_group_earlier
        },
    )
}
