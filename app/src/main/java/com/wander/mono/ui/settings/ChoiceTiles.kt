package com.wander.mono.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.components.expressivePress

/**
 * [count] tiles as an even grid: as many to a line as fit at [TileMinWidth], the lines balanced
 * (five tiles three and two, never four and one), a short last line centred. Each line is as tall
 * as its tallest tile, so a two-line caption never leaves its neighbours shorter.
 */
@Composable
internal fun ChoiceTiles(count: Int, modifier: Modifier = Modifier, tile: @Composable (index: Int) -> Unit) {
    Layout(content = { repeat(count) { tile(it) } }, modifier = modifier) { measurables, constraints ->
        val gap = TileGap.roundToPx()
        val min = TileMinWidth.roundToPx()
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else count * (min + gap)
        if (count == 0) return@Layout layout(width, 0) {}
        val fit = ((width + gap) / (min + gap)).coerceIn(1, count)
        val lines = (count + fit - 1) / fit
        val columns = (count + lines - 1) / lines
        val tileWidth = ((width - gap * (columns - 1)) / columns).coerceAtLeast(0)
        val rows = measurables.chunked(columns).map { line ->
            val height = line.maxOf { it.maxIntrinsicHeight(tileWidth) }
            line.map { it.measure(Constraints.fixed(tileWidth, height)) }
        }
        val height = rows.sumOf { it.first().height } + gap * (rows.size - 1)
        layout(width, height) {
            var y = 0
            rows.forEach { line ->
                val used = line.size * tileWidth + gap * (line.size - 1)
                var x = (width - used) / 2
                line.forEach { p ->
                    p.placeRelative(x, y)
                    x += tileWidth + gap
                }
                y += line.first().height + gap
            }
        }
    }
}

/**
 * One value as a tile: its [preview] over its [label], or with [captions] off the preview alone,
 * the label then its spoken name. The chosen tile takes the secondary container, rounds its
 * corners further and lifts its preview a little, all on the theme's motion. [preview] draws in
 * the tile's content colour ([LocalContentColor]).
 */
@Composable
internal fun ChoiceTileItem(
    selected: Boolean,
    label: String,
    captions: Boolean,
    onClick: () -> Unit,
    preview: @Composable () -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(
        if (selected) colors.secondaryContainer else colors.surfaceContainerHighest,
        motion.fastEffectsSpec(),
        label = "choiceTile",
    )
    val content by animateColorAsState(
        if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant,
        motion.fastEffectsSpec(),
        label = "choiceTileContent",
    )
    val corner by animateDpAsState(if (selected) TileCornerChosen else TileCorner, motion.fastSpatialSpec())
    val lift by animateFloatAsState(if (selected) PREVIEW_LIFT else 1f, motion.fastSpatialSpec())
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(corner)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .expressivePress(interaction)
            .clip(shape)
            .background(container, shape)
            .selectable(selected, interaction, ripple(), role = Role.RadioButton, onClick = onClick)
            .then(if (captions) Modifier else Modifier.semantics { contentDescription = label })
            .padding(horizontal = 6.dp, vertical = 12.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides content) {
            Box(
                Modifier.size(PreviewSize).graphicsLayer {
                    scaleX = lift
                    scaleY = lift
                },
                contentAlignment = Alignment.Center,
            ) { preview() }
            if (captions) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        }
    }
}

/**
 * A [ChoiceSetting] preview of a shape, filled in the tile's colour: an icon mask, say. Null
 * stands for "the system's own", drawn as a rounded square's outline.
 */
@Composable
fun ChoiceShapePreview(shape: Shape?) {
    val color = LocalContentColor.current
    val modifier = Modifier.size(PreviewShapeSize)
    if (shape == null) {
        Box(modifier.border(OUTLINE_WIDTH.dp, color, RoundedCornerShape(SYSTEM_CORNER_PERCENT)))
    } else {
        Box(modifier.background(color, shape))
    }
}

/** A [ChoiceSetting] preview of a colour: a round swatch, ringed so a pale one still shows. */
@Composable
fun ChoiceSwatchPreview(color: Color) {
    val ring = LocalContentColor.current.copy(alpha = SWATCH_RING_ALPHA)
    Box(Modifier.size(PreviewShapeSize).background(color, CircleShape).border(1.dp, ring, CircleShape))
}

/** A [ChoiceSetting] preview drawn as a glyph in the tile's colour. */
@Composable
fun ChoiceIconPreview(@DrawableRes icon: Int) {
    Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(PreviewGlyphSize))
}

/** The narrowest a tile gets before the grid starts a new line. */
private val TileMinWidth = 68.dp
private val TileGap = 8.dp
private val TileCorner = 16.dp
private val TileCornerChosen = 26.dp
private val PreviewSize = 40.dp
private val PreviewShapeSize = 32.dp
private val PreviewGlyphSize = 26.dp
private const val PREVIEW_LIFT = 1.12f
private const val OUTLINE_WIDTH = 2
private const val SYSTEM_CORNER_PERCENT = 30
private const val SWATCH_RING_ALPHA = 0.3f
