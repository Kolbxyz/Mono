package com.wander.mono.ui.settings

import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

/**
 * [count] choice buttons laid out by what fits. Every label at its full one-line width on one row:
 * a connected button group, as wide as the row, the buttons equal when the widest allows it.
 * Otherwise (or with [wrap]) standalone pills on as many lines as they need. [button] draws value
 * `index` with the connected [ToggleButtonShapes] of its place, or null for a standalone pill.
 *
 * The measure decides; the shapes follow it on the next frame, so a group that stops fitting
 * (a larger font, a narrower window) is laid out right at once and reshaped a frame later.
 */
@Composable
internal fun ChoiceGroup(
    count: Int,
    wrap: Boolean,
    modifier: Modifier = Modifier,
    button: @Composable (index: Int, shapes: ToggleButtonShapes?, modifier: Modifier) -> Unit,
) {
    var wrapped by remember { mutableStateOf(wrap) }
    val connected = !wrap && !wrapped
    val leading = ButtonGroupDefaults.connectedLeadingButtonShapes()
    val middle = ButtonGroupDefaults.connectedMiddleButtonShapes()
    val trailing = ButtonGroupDefaults.connectedTrailingButtonShapes()
    Layout(
        content = {
            repeat(count) { index ->
                val shapes = when {
                    !connected -> null
                    index == 0 -> leading
                    index == count - 1 -> trailing
                    else -> middle
                }
                button(index, shapes, Modifier)
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val tight = ButtonGroupDefaults.ConnectedSpaceBetween.roundToPx()
        val natural = measurables.map { it.maxIntrinsicWidth(Constraints.Infinity) }
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else natural.sum() + tight * (count - 1)
        val widths = if (wrap) null else rowWidths(natural, width, tight)
        if (wrapped != (widths == null)) wrapped = widths == null
        if (widths != null) connectedRow(measurables, widths, width, tight, constraints) else pills(measurables, width)
    }
}

/**
 * Each button's width when all [natural] widths share one [width] row with [gap] between: equal
 * when the widest times their count fits, else each its own plus an even share of what is left;
 * null when they do not fit at all.
 */
internal fun rowWidths(natural: List<Int>, width: Int, gap: Int): List<Int>? {
    val room = width - gap * (natural.size - 1)
    val left = room - natural.sum()
    return when {
        natural.isEmpty() -> emptyList()
        natural.max() * natural.size <= room -> evenSplit(room, natural.size)
        left < 0 -> null
        else -> evenSplit(left, natural.size).let { shares -> natural.mapIndexed { i, w -> w + shares[i] } }
    }
}

/** [total] cut into [parts] that differ by at most one, the remainder going to the first ones. */
private fun evenSplit(total: Int, parts: Int): List<Int> =
    List(parts) { i -> total / parts + if (i < total % parts) 1 else 0 }

private fun MeasureScope.connectedRow(
    measurables: List<Measurable>,
    widths: List<Int>,
    width: Int,
    gap: Int,
    constraints: Constraints,
): MeasureResult {
    val placeables = measurables.mapIndexed { i, m ->
        m.measure(Constraints(widths[i], widths[i], 0, constraints.maxHeight))
    }
    val height = placeables.maxOfOrNull { it.height } ?: 0
    return layout(width, height) {
        var x = 0
        placeables.forEach { p ->
            p.placeRelative(x, (height - p.height) / 2)
            x += p.width + gap
        }
    }
}

/** Standalone pills, each at its own width, filling line after line from the start edge. */
private fun MeasureScope.pills(measurables: List<Measurable>, width: Int): MeasureResult {
    val gap = PillGap.roundToPx()
    val placeables = measurables.map { it.measure(Constraints(maxWidth = width)) }
    val lines = mutableListOf<MutableList<Int>>()
    var used = 0
    placeables.forEachIndexed { i, p ->
        val line = lines.lastOrNull()
        if (line == null || used + gap + p.width > width) {
            lines += mutableListOf(i)
            used = p.width
        } else {
            line += i
            used += gap + p.width
        }
    }
    val heights = lines.map { line -> line.maxOf { placeables[it].height } }
    val height = heights.sum() + gap * (lines.size - 1).coerceAtLeast(0)
    return layout(width, height) {
        var y = 0
        lines.forEachIndexed { row, line ->
            var x = 0
            line.forEach { i ->
                val p = placeables[i]
                p.placeRelative(x, y + (heights[row] - p.height) / 2)
                x += p.width + gap
            }
            y += heights[row] + gap
        }
    }
}

/** The space between standalone pills, across and down. */
private val PillGap = 8.dp
