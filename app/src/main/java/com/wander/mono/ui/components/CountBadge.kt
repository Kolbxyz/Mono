package com.wander.mono.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import com.wander.mono.R

/**
 * The unread-count pill. A rising count pops in from 0.8 scale; a falling one just changes, since
 * reading a message is not news. Its meaning is announced by the row, not by the pill.
 */
@Composable
fun CountBadge(badge: AppBadge.Count, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(1f) }
    var previous by remember { mutableIntStateOf(badge.count) }
    val spec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    LaunchedEffect(badge.count) {
        if (badge.count > previous) {
            scale.snapTo(POP_FROM)
            scale.animateTo(1f, spec)
        }
        previous = badge.count
    }
    val text = if (badge.isCapped) {
        stringResource(R.string.badge_count_capped, badge.shown)
    } else {
        stringResource(R.string.badge_count, badge.shown)
    }
    Badge(
        modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        Text(text)
    }
}

private const val POP_FROM = 0.8f
