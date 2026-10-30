package com.wander.mono.ui.drawer

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * The list melts into the drawer's background at its ends instead of being cut off: at the top while
 * it can scroll back, at the bottom while it can scroll on, so a list at rest shows
 * its first and last rows whole. Drawn as a mask over the list alone, so it fades to whatever lies
 * behind (wallpaper, blur or surface) and never tints it.
 */
internal fun Modifier.listFadingEdges(state: LazyListState): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val topPx = if (state.canScrollBackward) TOP_FADE.toPx() else 0f
            val bottomPx = if (state.canScrollForward) BOTTOM_FADE.toPx() else 0f
            if (topPx > 0f) {
                drawRect(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black), startY = 0f, endY = topPx),
                    size = Size(size.width, topPx),
                    blendMode = BlendMode.DstIn,
                )
            }
            if (bottomPx > 0f) {
                val from = size.height - bottomPx
                drawRect(
                    Brush.verticalGradient(listOf(Color.Black, Color.Transparent), startY = from, endY = size.height),
                    topLeft = Offset(0f, from),
                    size = Size(size.width, bottomPx),
                    blendMode = BlendMode.DstIn,
                )
            }
        }

/** How far down the list the top edge fades in: a row's top third, enough to read as a soft edge. */
private val TOP_FADE = 20.dp

/** The bottom fades over more, under the floating toolbar and the gesture bar. */
private val BOTTOM_FADE = 48.dp
