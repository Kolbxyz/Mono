package com.wander.mono.ui.home.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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

/** Which app plays: its small icon and name on a pill of the card's content colour; a tap opens it. */
@Composable
internal fun PlayerAppChip(app: LaunchableApp, icons: AppIcons, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val content = LocalContentColor.current
    Row(
        modifier
            .heightIn(min = CHIP_HEIGHT)
            .clip(CircleShape)
            .background(content.copy(alpha = CHIP_VEIL))
            .clickable(
                onClickLabel = stringResource(R.string.media_open_app, app.label),
                role = Role.Button,
                onClick = onOpen,
            )
            .padding(start = CHIP_START, end = CHIP_END),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CHIP_START),
    ) {
        AppIcon(app.key, icons, size = CHIP_ICON)
        Text(app.label, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private val CHIP_HEIGHT = 28.dp
private val CHIP_ICON = 18.dp
private val CHIP_START = 6.dp
private val CHIP_END = 12.dp

/** The chip's pill: a light veil of the content colour, like the transport's skip buttons. */
private const val CHIP_VEIL = 0.16f
