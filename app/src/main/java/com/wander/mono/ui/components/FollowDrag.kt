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
 * Something a vertical drag moves with the finger, such as the drawer: [dragBy] takes each delta
 * in pixels (negative is up) and [release] the finger's velocity when it lifts, to settle.
 */
interface FollowDrag {
    val isDragging: Boolean

    fun dragBy(deltaY: Float)

    fun release(velocityY: Float)
}

/**
 * Home's swipes. Up, past the end of the list when it scrolls, drags [drag] with the finger (and
 * back down while that same drag lasts). Down, past the top of the list, fires [onPullDown] once
 * per gesture after a short distance, as it hands off to something that cannot follow the finger.
 */
@Composable
fun rememberPullUpConnection(drag: FollowDrag, onPullDown: (() -> Unit)?): NestedScrollConnection {
    val threshold = with(LocalDensity.current) { SWIPE_DISTANCE.toPx() }
    val currentOnPullDown = rememberUpdatedState(onPullDown)
    return remember(drag, threshold) {
        object : NestedScrollConnection {
            private var pulled = 0f
            private var fired = false

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput || !drag.isDragging) return Offset.Zero
                drag.dragBy(available.y)
                return Offset(0f, available.y)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                val taken = if (available.y < 0f) {
                    drag.dragBy(available.y)
                    available.y
                } else {
                    pullDownBy(available.y, consumed.y)
                }
                return Offset(0f, taken)
            }

            /**
             * Counts a downward pull past the list's top, firing the pull-down action once per gesture.
             * Returns what it took: all of it while there is a pull-down action, so the list does not
             * also stretch under the gesture.
             */
            private fun pullDownBy(leftover: Float, consumed: Float): Float {
                val pullDown = currentOnPullDown.value
                if (leftover > 0f && pullDown != null) {
                    pulled += leftover
                    if (pulled >= threshold && !fired) {
                        fired = true
                        pullDown()
                    }
                    return leftover
                }
                if (consumed != 0f) pulled = 0f
                return 0f
            }

            override suspend fun onPreFling(available: Velocity): Velocity = releaseFling(available)

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
                releaseFling(available)

            private fun releaseFling(available: Velocity): Velocity {
                pulled = 0f
                fired = false
                if (!drag.isDragging) return Velocity.Zero
                drag.release(available.y)
                return available
            }
        }
    }
}

/**
 * The drawer's swipe down: past the top of its list (or anywhere nothing scrolls) the drag moves
 * [drag] with the finger, back up too while the same drag lasts, and the fling settles it.
 */
@Composable
fun rememberPullDownConnection(drag: FollowDrag): NestedScrollConnection = remember(drag) {
    object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (source != NestedScrollSource.UserInput || !drag.isDragging) return Offset.Zero
            drag.dragBy(available.y)
            return Offset(0f, available.y)
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (source != NestedScrollSource.UserInput || available.y <= 0f) return Offset.Zero
            drag.dragBy(available.y)
            return Offset(0f, available.y)
        }

        override suspend fun onPreFling(available: Velocity): Velocity = releaseFling(available)

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = releaseFling(available)

        private fun releaseFling(available: Velocity): Velocity {
            if (!drag.isDragging) return Velocity.Zero
            drag.release(available.y)
            return available
        }
    }
}

private val SWIPE_DISTANCE = 72.dp
