package com.wander.mono.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    // Snapshot state: a re-layout that moves the strips at the same total height still redraws them.
    var strips by remember { mutableStateOf(emptyList<Strip>()) }
    Layout(
        content = content,
        modifier = modifier
            .clip(RoundedCornerShape(SegmentOuter))
            .drawBehind {
                strips.forEachIndexed { i, strip ->
                    val outline = segmentedShape(i, strips.size)
                        .createOutline(Size(size.width, strip.height.toFloat()), layoutDirection, this)
                    translate(top = strip.top.toFloat()) { drawOutline(outline, color) }
                }
            },
    ) { measurables, constraints ->
        val gap = SegmentGap.roundToPx()
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else constraints.minWidth
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = width, minHeight = 0)) }
        val laidOut = stackStrips(placeables.map { it.height }, gap)
        strips = laidOut
        val height = laidOut.lastOrNull()?.let { it.top + it.height } ?: 0
        layout(width, height) { placeables.forEachIndexed { i, placeable -> placeable.place(0, laidOut[i].top) } }
    }
}

/** Where one strip was laid out, read back when drawing its background. */
internal data class Strip(val top: Int, val height: Int)

/** Strips of these [heights] stacked top down with [gap] between them. */
internal fun stackStrips(heights: List<Int>, gap: Int): List<Strip> {
    var y = 0
    return heights.map { height -> Strip(y, height).also { y += height + gap } }
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
