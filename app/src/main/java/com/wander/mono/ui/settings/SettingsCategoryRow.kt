package com.wander.mono.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.components.instantPress
import com.wander.mono.ui.components.pressShrink

/**
 * One place on the settings hub: a shaped badge in a theme colour, the title, and one short line of
 * what is inside. Under the finger the row shrinks a little and the badge relaxes into a circle;
 * both on the theme's motion. The badge and row show the press, so no ripple is drawn over them.
 */
@Composable
internal fun SettingsCategoryRow(category: SettingsCategory, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val shape = rememberPressMorphShape(category.shape, MaterialShapes.Circle, isPressed)
    val (container, glyph) = category.colors(MaterialTheme.colorScheme)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .pressShrink(interaction)
            .instantPress(interaction)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(Modifier.size(BadgeSize).background(container, shape), contentAlignment = Alignment.Center) {
            Icon(
                painterResource(category.icon),
                contentDescription = null,
                tint = glyph,
                modifier = Modifier.size(GlyphSize),
            )
        }
        Column(Modifier.padding(start = 16.dp)) {
            Text(stringResource(category.title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(category.subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

private val BadgeSize = 56.dp
private val GlyphSize = 28.dp
