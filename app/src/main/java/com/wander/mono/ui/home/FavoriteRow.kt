package com.wander.mono.ui.home

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.components.AppLabel

/**
 * A favourite: just its name in large type, at least 56 dp tall (spec 4.3). Tap opens it,
 * long-press opens its options. TalkBack reads it as one node ("Phalanx, 3 unread") and offers App
 * info and the moves it can make as custom actions, so reordering needs no sheet.
 */
@Composable
fun FavoriteRow(
    row: HomeRow.App,
    onLaunch: () -> Unit,
    onLongPress: () -> Unit,
    onAppInfo: () -> Unit,
    onMove: (by: Int) -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    modifier: Modifier = Modifier,
) {
    val actions = favoriteActions(canMoveUp, canMoveDown, onAppInfo, onMove)
    AppLabel(
        app = row.app,
        badge = row.badge,
        style = MaterialTheme.typography.headlineSmall.onWallpaper(),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClickLabel = stringResource(R.string.action_open),
                onLongClickLabel = stringResource(R.string.action_app_options),
                onLongClick = onLongPress,
                onClick = onLaunch,
            )
            .semantics { customActions = actions }
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(vertical = 8.dp),
    )
}

@Composable
private fun favoriteActions(
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onAppInfo: () -> Unit,
    onMove: (by: Int) -> Unit,
): List<CustomAccessibilityAction> {
    val info = stringResource(R.string.sheet_app_info)
    val up = stringResource(R.string.sheet_move_up)
    val down = stringResource(R.string.sheet_move_down)
    return buildList {
        add(action(info, onAppInfo))
        if (canMoveUp) add(action(up) { onMove(-1) })
        if (canMoveDown) add(action(down) { onMove(1) })
    }
}

private fun action(label: String, run: () -> Unit) = CustomAccessibilityAction(label) {
    run()
    true
}
