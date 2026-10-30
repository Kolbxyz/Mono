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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
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
 * The app's shortcuts under "Open", as a row of tonal chips that scrolls sideways: each its own
 * icon (its app's when it has none, nothing in text-only mode) and label. A tap launches it.
 */
@Composable
internal fun ShortcutRow(
    app: AppKey,
    shortcuts: List<AppShortcut>,
    icons: AppIcons,
    loadIcon: SheetShortcutIcon,
    onShortcut: (AppShortcut) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(shortcuts) { shortcut ->
            ShortcutChip(shortcut, app, icons, loadIcon) { onShortcut(shortcut) }
        }
    }
}

@Composable
private fun ShortcutChip(
    shortcut: AppShortcut,
    app: AppKey,
    icons: AppIcons,
    loadIcon: SheetShortcutIcon,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(ChipCorner),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        interactionSource = interaction,
        modifier = Modifier
            .height(ChipHeight)
            .expressivePress(interaction, PressMorph.squarer(ChipCorner))
            .instantPress(interaction),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            ShortcutGlyph(shortcut, app, icons, loadIcon)
            Text(
                shortcut.label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = LabelMaxWidth),
            )
        }
    }
}

/** Decorative: the chip's label names the shortcut. */
@Composable
private fun ShortcutGlyph(shortcut: AppShortcut, app: AppKey, icons: AppIcons, loadIcon: SheetShortcutIcon) {
    if (icons.mode == IconMode.TextOnly) return
    val sizePx = with(LocalDensity.current) { GlyphSize.roundToPx() }
    val own by produceState<AppIconImage?>(null, shortcut, sizePx) { value = loadIcon(shortcut, sizePx) }
    val image = own
    if (image == null) {
        AppIcon(app, icons, size = GlyphSize)
    } else {
        Image(image.bitmap, contentDescription = null, modifier = Modifier.size(GlyphSize).clip(CircleShape))
    }
}

private val ChipHeight = 48.dp
private val ChipCorner = 16.dp
private val GlyphSize = 24.dp
private val LabelMaxWidth = 160.dp
