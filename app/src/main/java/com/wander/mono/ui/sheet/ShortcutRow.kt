package com.wander.mono.ui.sheet

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.PressMorph
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.components.instantPress

/** Loads a shortcut's own icon (a face, an avatar) at a pixel size; null when it has none. */
typealias SheetShortcutIcon = suspend (AppShortcut, Int) -> AppIconImage?

/**
 * The app's shortcuts, as a row of tonal chips that scrolls sideways: each its own icon (its app's
 * when it has none, nothing in text-only mode) and label. A tap launches it. The sheet shows them
 * under "Open"; home unfolds them [small] under a favourite, which takes [contentPadding] to line
 * them up with its name.
 */
@Composable
internal fun ShortcutRow(
    app: AppKey,
    shortcuts: List<AppShortcut>,
    icons: AppIcons,
    loadIcon: SheetShortcutIcon,
    onShortcut: (AppShortcut) -> Unit,
    modifier: Modifier = Modifier,
    small: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    val size = if (small) SmallChip else RegularChip
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(shortcuts) { shortcut ->
            ShortcutChip(shortcut, app, icons, loadIcon, size) { onShortcut(shortcut) }
        }
    }
}

@Composable
private fun ShortcutChip(
    shortcut: AppShortcut,
    app: AppKey,
    icons: AppIcons,
    loadIcon: SheetShortcutIcon,
    size: ChipSize,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    // Below 48 dp tall, the Surface still keeps a 48 dp touch target around the small chip.
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(size.corner),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        interactionSource = interaction,
        modifier = Modifier
            .height(size.height)
            .expressivePress(interaction, PressMorph.squarer(size.corner))
            .instantPress(interaction),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            ShortcutGlyph(shortcut, app, icons, loadIcon, size.glyph)
            Text(
                shortcut.label,
                style = size.label(MaterialTheme.typography),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = LabelMaxWidth),
            )
        }
    }
}

/** Decorative: the chip's label names the shortcut. */
@Composable
private fun ShortcutGlyph(
    shortcut: AppShortcut,
    app: AppKey,
    icons: AppIcons,
    loadIcon: SheetShortcutIcon,
    glyphSize: Dp,
) {
    if (icons.mode == IconMode.TextOnly) return
    val sizePx = with(LocalDensity.current) { glyphSize.roundToPx() }
    val own by produceState<AppIconImage?>(null, shortcut, sizePx) { value = loadIcon(shortcut, sizePx) }
    val image = own
    if (image == null) {
        AppIcon(app, icons, size = glyphSize)
    } else {
        Image(image.bitmap, contentDescription = null, modifier = Modifier.size(glyphSize).clip(CircleShape))
    }
}

/** A chip's measures: the sheet's regular one, or home's small one under a favourite. */
private class ChipSize(val height: Dp, val corner: Dp, val glyph: Dp, val label: (Typography) -> TextStyle)

private val RegularChip = ChipSize(height = 48.dp, corner = 16.dp, glyph = 24.dp, label = { it.labelLarge })
private val SmallChip = ChipSize(height = 36.dp, corner = 12.dp, glyph = 20.dp, label = { it.labelMedium })
private val LabelMaxWidth = 160.dp
