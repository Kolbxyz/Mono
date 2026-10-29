package com.wander.mono.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The corners of one strip of a segmented group: the group's outer corners are round and the
 * seams between strips are tight, so a run of rows reads as one card cut into strips.
 */
fun segmentedShape(index: Int, count: Int, outer: Dp = SegmentOuter, inner: Dp = SegmentInner): Shape {
    val top = if (index == 0) outer else inner
    val bottom = if (index == count - 1) outer else inner
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

/**
 * Stacks its children as the strips of one segmented group, each on a [color] strip with
 * [segmentedShape] corners and [SegmentGap] between. The children need not know their place: rows
 * that draw nothing (a setting not loaded yet) simply leave no strip. Ripples stay inside the
 * group's rounded outline.
 */
@Composable
fun SegmentedColumn(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable () -> Unit,
) {
    val strips = remember { Strips() }
    Layout(
        content = content,
        modifier = modifier
            .clip(RoundedCornerShape(SegmentOuter))
            .drawBehind {
                val count = strips.tops.size
                for (i in 0 until count) {
                    val outline = segmentedShape(i, count)
                        .createOutline(Size(size.width, strips.heights[i].toFloat()), layoutDirection, this)
                    translate(top = strips.tops[i].toFloat()) { drawOutline(outline, color) }
                }
            },
    ) { measurables, constraints ->
        val gap = SegmentGap.roundToPx()
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else constraints.minWidth
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = width, minHeight = 0)) }
        var y = 0
        val tops = IntArray(placeables.size)
        placeables.forEachIndexed { i, placeable ->
            tops[i] = y
            y += placeable.height + if (i < placeables.lastIndex) gap else 0
        }
        strips.tops = tops
        strips.heights = IntArray(placeables.size) { placeables[it].height }
        layout(width, y) { placeables.forEachIndexed { i, placeable -> placeable.place(0, tops[i]) } }
    }
}

/** Where the strips were laid out, read back when drawing their backgrounds. */
private class Strips {
    var tops = IntArray(0)
    var heights = IntArray(0)
}

/** Corner radius of a segmented group's outer edges. */
val SegmentOuter = 26.dp

/** Corner radius at the seams between strips. */
val SegmentInner = 8.dp

/** The gap between the strips of a segmented group. */
val SegmentGap = 2.dp

/**
 * Draws one item of a lazy list as strip [index] of a [count]-strip segmented group: same corners
 * and colour as [SegmentedColumn]. Put [SegmentGap] between items with the list's arrangement.
 */
@Composable
fun Modifier.groupedListItem(
    index: Int,
    count: Int,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
): Modifier {
    val shape = segmentedShape(index, count)
    return clip(shape).background(color, shape)
}
