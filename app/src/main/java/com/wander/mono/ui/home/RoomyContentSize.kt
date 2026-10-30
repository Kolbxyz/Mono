package com.wander.mono.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.offset

/**
 * [animateContentSize] for text on the wallpaper. Its clip sits [room] outside the bounds on every
 * side, so the text shadow or halo past the edges is not cut; the layout size is unchanged.
 */
internal fun Modifier.roomyContentSize(spec: FiniteAnimationSpec<IntSize>, room: Dp): Modifier =
    layout { measurable, constraints ->
        val extra = room.roundToPx()
        val placeable = measurable.measure(constraints.offset(2 * extra, 2 * extra))
        val width = (placeable.width - 2 * extra).coerceAtLeast(0)
        val height = (placeable.height - 2 * extra).coerceAtLeast(0)
        layout(width, height) { placeable.place(-extra, -extra) }
    }
        .animateContentSize(spec)
        .padding(room)
