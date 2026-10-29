package com.wander.mono.ui.sheet

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.components.pressShrink

/** A row of [SheetTile]s at the top of a sheet: the actions people reach for most, one tap away. */
@Composable
internal fun SheetTileRow(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(TILE_GAP),
        content = content,
    )
}

/**
 * A tall tonal tile, its symbol over a short label, as in Phalanx's long-press menu. Its corners
 * tighten while held. [emphasis] puts it on the primary container: the first tile, or a mode
 * that is on. [destructive] only colours the symbol and label, so it warns without shouting.
 */
@Composable
internal fun RowScope.SheetTile(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    emphasis: Boolean = false,
    destructive: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val scheme = MaterialTheme.colorScheme
    val colors = when {
        destructive -> ButtonDefaults.filledTonalButtonColors(scheme.surfaceContainerHighest, scheme.error)
        emphasis -> ButtonDefaults.filledTonalButtonColors(scheme.primaryContainer, scheme.onPrimaryContainer)
        else -> ButtonDefaults.filledTonalButtonColors(scheme.surfaceContainerHighest, scheme.onSurface)
    }
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.weight(1f).heightIn(min = TILE_HEIGHT).pressShrink(interaction, TILE_PRESSED_SCALE),
        shapes = ButtonDefaults.shapes(MaterialTheme.shapes.large, MaterialTheme.shapes.small),
        colors = colors,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
        interactionSource = interaction,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(painterResource(icon), contentDescription = null)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** One action of a sheet, as a strip of a segmented group: an optional symbol and its label. */
@Composable
internal fun SheetAction(label: String, @DrawableRes icon: Int? = null, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
        leadingContent = icon?.let { { Icon(painterResource(it), contentDescription = null) } },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    ) {
        Text(label, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

private val TILE_HEIGHT = 88.dp
private val TILE_GAP = 8.dp
private const val TILE_PRESSED_SCALE = 0.94f
