package com.wander.mono.ui.edit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.wander.mono.R

/** How far a dragged row has risen, 0 at rest to 1 lifted, on the motion scheme's fast spatial spec. */
@Composable
internal fun animateLift(lifted: Boolean): State<Float> =
    animateFloatAsState(if (lifted) 1f else 0f, MaterialTheme.motionScheme.fastSpatialSpec(), label = "lift")

/** A slight scale while lifted; for the whole dragged item, its cards and placeholders alike. */
internal fun Modifier.liftScale(lift: State<Float>): Modifier = graphicsLayer {
    val scale = 1f + LIFT_SCALE * lift.value
    scaleX = scale
    scaleY = scale
}

/**
 * The container of an edit card: larger corners, a shadow and a brighter surface as it lifts.
 * The shadow is drawn by this layer, so the card is clipped to the same shape.
 */
@Composable
internal fun Modifier.editCard(lift: State<Float>): Modifier {
    val scheme = MaterialTheme.colorScheme
    return graphicsLayer {
        shadowElevation = LIFT_ELEVATION.toPx() * lift.value
        shape = RoundedCornerShape(lerp(REST_CORNER, LIFTED_CORNER, lift.value))
        clip = true
    }.background(lerp(scheme.surfaceContainerHigh, scheme.surfaceContainerHighest, lift.value))
}

/** The grip a row is dragged by; [handle] carries its gesture. */
@Composable
internal fun DragHandle(handle: Modifier) {
    Icon(
        painterResource(R.drawable.sym_drag_indicator),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = handle.padding(12.dp).size(24.dp),
    )
}

private const val LIFT_SCALE = 0.03f
private val LIFT_ELEVATION = 8.dp
private val REST_CORNER = 16.dp
private val LIFTED_CORNER = 28.dp
