package com.wander.mono.ui.home

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.wander.mono.data.settings.GlanceLines
import kotlin.math.roundToInt

/**
 * The clock grown by the hero pull at [progress]: drawn scaled from its top edge (its start, or its
 * middle when [centred]) and taking the grown height in the layout, so the lines under it move
 * down with it instead of being covered. Read at layout time: a pull relayouts the header only.
 */
internal fun Modifier.heroGrow(progress: () -> Float, centred: Boolean): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val room = if (constraints.hasBoundedWidth && placeable.width > 0) {
        constraints.maxWidth / placeable.width.toFloat()
    } else {
        1f
    }
    val scale = heroScale(progress(), room)
    layout(placeable.width, (placeable.height * scale).roundToInt().coerceAtLeast(0)) {
        placeable.placeWithLayer(0, 0) {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(if (centred) MIDDLE else 0f, 0f)
        }
    }
}

/**
 * The summary under the header at [progress]: it takes that share of its height, so the header
 * opens to make room as the clock grows, and fades and drops in with it.
 */
internal fun Modifier.heroReveal(progress: () -> Float): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val shown = progress().coerceIn(0f, 1f)
    val drop = REVEAL_DROP.toPx()
    layout(placeable.width, (placeable.height * shown).roundToInt()) {
        placeable.placeWithLayer(0, 0) {
            alpha = shown
            translationY = (shown - 1f) * drop
        }
    }
}

/**
 * How far the hero has grown for [pulled] px of a pull over [distance]: quick off the mark and
 * easing in to full, so a gentle pull already shows something and the end feels like a stop.
 */
internal fun heroProgress(pulled: Float, distance: Float): Float {
    if (distance <= 0f) return 0f
    val t = (pulled / distance).coerceIn(0f, 1f)
    return 1f - (1f - t) * (1f - t)
}

/**
 * The clock's scale at [progress]: up to [HERO_MAX_SCALE], but never wider than the [room] it has
 * (the available width over its own); a settling bounce below 0 dips it just under its size.
 */
internal fun heroScale(progress: Float, room: Float): Float = lerp(1f, room.coerceIn(1f, HERO_MAX_SCALE), progress)

/**
 * The lines the large, simple preset leaves over the list: the clock and the date only, so the
 * rows get the room. The rest is still a pull away, in the summary ([summaryLines]).
 */
internal fun shownLines(lines: GlanceLines, simple: Boolean): GlanceLines =
    if (simple) GlanceLines(date = lines.date, alarm = false, event = false, battery = false) else lines

/** The hero's "today" summary: every line the header is not already showing. */
internal fun summaryLines(shown: GlanceLines): GlanceLines =
    GlanceLines(date = !shown.date, alarm = !shown.alarm, event = !shown.event, battery = !shown.battery)

/** The display size the hero grows the clock toward, as a share of its own. */
internal const val HERO_MAX_SCALE = 1.5f

/** Halfway across, as a transform origin's fraction of the width. */
private const val MIDDLE = 0.5f

/** How far the summary drops into place as it appears. */
private val REVEAL_DROP = 8.dp
