package com.wander.mono.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

/**
 * Fires [onSwipe] when a drag keeps going past the end of a list that cannot scroll further: up
 * past the bottom when [upward], down past the top otherwise. Built on nested scroll, so the list
 * keeps every normal scroll and only the leftover drag counts.
 */
@Composable
fun rememberOverscrollSwipe(upward: Boolean, onSwipe: () -> Unit): NestedScrollConnection {
    val threshold = with(LocalDensity.current) { SWIPE_DISTANCE.toPx() }
    val currentOnSwipe = rememberUpdatedState(onSwipe)
    return remember(upward, threshold) {
        object : NestedScrollConnection {
            private var pulled = 0f

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                val leftover = if (upward) -available.y else available.y
                if (leftover > 0f) {
                    pulled += leftover
                    if (pulled >= threshold) {
                        pulled = 0f
                        currentOnSwipe.value()
                    }
                } else if (consumed.y != 0f) {
                    pulled = 0f
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                pulled = 0f
                return Velocity.Zero
            }
        }
    }
}

private val SWIPE_DISTANCE = 72.dp
