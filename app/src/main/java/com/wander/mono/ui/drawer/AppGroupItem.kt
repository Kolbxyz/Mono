package com.wander.mono.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.home.onWallpaper
import com.wander.mono.ui.theme.schemeAnimateItem

/**
 * The list's [groups], one item each so the rail's indexes match the list's, every row making its
 * [entrance] in order from the top of the screen as the drawer opens. A group [shows] is false for
 * fades out, while a finger holds another letter on the rail.
 */
internal fun LazyListScope.appGroupItems(
    groups: List<AppGroup>,
    icons: AppIcons,
    listState: LazyListState,
    entrance: ListEntrance,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    shows: (Int) -> Boolean = { true },
) {
    // Where each group's heading falls in a count of headings and rows from the top of the list.
    val starts = groups.runningFold(0) { at, group -> at + 1 + group.entries.size }
    itemsIndexed(groups, key = { _, group -> group.key }) { index, group ->
        // Read once, as the group composes, and not observed (scrolling must not recompose it): rows
        // count from the first on screen, not the top of the list.
        val firstShown = remember {
            Snapshot.withoutReadObservation { starts.getOrElse(listState.firstVisibleItemIndex) { 0 } }
        }
        AppGroupItem(
            group,
            icons,
            onLaunchApp,
            onLongPress,
            rowEntrance = { row -> entranceMotion(entrance, starts[index] + row - firstShown) },
            modifier = schemeAnimateItem().heldFade(shows(index)),
        )
    }
}

/**
 * One group of the drawer's list: its heading, then its apps straight on the drawer's background,
 * each making its [rowEntrance] (0 the heading, then the rows).
 */
@Composable
private fun AppGroupItem(
    group: AppGroup,
    icons: AppIcons,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    rowEntrance: @Composable (Int) -> Modifier,
    modifier: Modifier = Modifier,
) {
    val labelStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium).onWallpaper()
    Column(modifier.padding(bottom = GROUP_GAP)) {
        GroupHeading(group.head, alignToIcons = icons.mode != IconMode.TextOnly, modifier = rowEntrance(0))
        group.entries.forEachIndexed { row, entry ->
            DrawerAppRow(
                entry = entry,
                icons = icons,
                onLaunch = { onLaunchApp(entry.app) },
                onLongPress = { onLongPress(entry.app) },
                modifier = rowEntrance(row + 1),
                labelStyle = labelStyle,
            )
        }
    }
}

/**
 * A letter, large and bold over the icons' column ([alignToIcons]; over the names in text-only
 * mode), quiet in primary; or a word ("New", led by the same tertiary dot its apps carry, or how
 * long ago) in a title over the names' start.
 */
@Composable
private fun GroupHeading(head: GroupHead, alignToIcons: Boolean, modifier: Modifier = Modifier) {
    val spaced = modifier
        .padding(start = 16.dp, end = 16.dp, top = HEADING_TOP, bottom = 4.dp)
        .semantics(mergeDescendants = true) { heading() }
    if (head is GroupHead.Letter) {
        Box(
            spaced.widthIn(min = if (alignToIcons) LETTER_COLUMN else 0.dp),
            contentAlignment = if (alignToIcons) Alignment.Center else Alignment.CenterStart,
        ) {
            Text(
                head.letter,
                style = MaterialTheme.typography.headlineSmall.onWallpaper(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    } else {
        Row(
            spaced,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (head == GroupHead.New) {
                Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape))
            }
            Text(
                headingText(head),
                style = MaterialTheme.typography.titleSmall.onWallpaper(),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
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

/** As wide as a row's icon, so a letter stands centred over the icons of its apps. */
private val LETTER_COLUMN = 40.dp

/** Air above a heading: the groups read as paragraphs, the rows in them close together. */
private val HEADING_TOP = 12.dp

/** Space after a group's last row, before the next heading. */
private val GROUP_GAP = 8.dp
