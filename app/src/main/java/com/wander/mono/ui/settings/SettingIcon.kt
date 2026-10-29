package com.wander.mono.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * A setting row's glyph on a plain tonal circle. Every row shares the same neutral tone: a single
 * setting has no colour of its own, and the hub's coloured badges stay the only colour on the
 * way in. The circle makes a page of rows read as a column of distinct controls.
 */
@Composable
fun SettingIconBadge(@DrawableRes icon: Int, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val alpha = if (enabled) 1f else DISABLED_ALPHA
    Box(
        modifier
            .size(BadgeSize)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = alpha), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
            modifier = Modifier.size(GlyphSize),
        )
    }
}

/** The leading slot of a setting row: the badge, or nothing for a row without an icon. */
internal fun leadingBadge(@DrawableRes icon: Int?, enabled: Boolean = true): (@Composable () -> Unit)? =
    icon?.let { { SettingIconBadge(it, enabled = enabled) } }

private val BadgeSize = 40.dp
private val GlyphSize = 22.dp

/** Material's disabled opacity. */
internal const val DISABLED_ALPHA = 0.38f
