package com.wander.mono.ui.home.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.home.HomePlayer
import com.wander.mono.ui.home.OutputChip

/**
 * The expanded player's header row: which app plays and, beside it, where it is heard, so the
 * output takes no row of its own. The output keeps a bounded width and the app name gives way
 * first; with neither, nothing is drawn.
 */
@Composable
internal fun PlayerChips(player: HomePlayer, icons: AppIcons, controls: MediaControls, modifier: Modifier = Modifier) {
    val app = player.app
    val output = player.media.output
    if (app == null && output == null) return
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING),
    ) {
        app?.let { PlayerAppChip(it, icons, controls::openPlayer, Modifier.weight(1f, fill = false)) }
        output?.let { OutputChip(it, controls::openOutputSwitcher, Modifier.widthIn(max = OUTPUT_MAX_WIDTH)) }
    }
}

/** Which app plays: its small icon and name on a pill of the card's content colour; a tap opens it. */
@Composable
private fun PlayerAppChip(app: LaunchableApp, icons: AppIcons, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    PlayerPill(
        onClickLabel = stringResource(R.string.media_open_app, app.label),
        onClick = onOpen,
        modifier = modifier,
    ) {
        AppIcon(app.key, icons, size = PillIconSize)
        PillLabel(app.label)
    }
}

/**
 * A small pill of the expanded player's header: a light veil of the card's content colour, as the
 * transport's skip buttons, holding an icon and a one-line label.
 */
@Composable
internal fun PlayerPill(
    onClickLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val veil = LocalContentColor.current.copy(alpha = PILL_VEIL)
    Row(
        modifier
            .heightIn(min = PILL_HEIGHT)
            .clip(CircleShape)
            .background(veil)
            .clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
            .padding(start = PILL_START, end = PILL_END),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PILL_START),
        content = content,
    )
}

/** A pill's label: one line, ellipsized when the header runs out of width. */
@Composable
internal fun PillLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** A pill's icon, the app's or the output's. */
internal val PillIconSize = 18.dp

private val PILL_HEIGHT = 28.dp
private val PILL_START = 6.dp
private val PILL_END = 12.dp
private val CHIP_SPACING = 6.dp

/** The output chip's widest, so a long device name never pushes the app chip out. */
private val OUTPUT_MAX_WIDTH = 132.dp

/** The pill's veil of the content colour, like the transport's skip buttons. */
private const val PILL_VEIL = 0.16f
