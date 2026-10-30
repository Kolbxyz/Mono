package com.wander.mono.ui.home

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates

/**
 * Where the scrim eases out: the header's bottom and the list viewport's top, measured in home's
 * content [onFrame], inside any scale applied to the content, so the drawer shrinking home back does
 * not move the scrim's stops. The callbacks fire in no set order, so each one measures again.
 */
@Stable
internal class ScrimEdges {
    private var frame: LayoutCoordinates? = null
    private var header: LayoutCoordinates? = null
    private var list: LayoutCoordinates? = null

    var headerBottom by mutableFloatStateOf(0f)
        private set

    var listTop by mutableFloatStateOf(0f)
        private set

    fun onFrame(coordinates: LayoutCoordinates) {
        frame = coordinates
        measure()
    }

    fun onHeader(coordinates: LayoutCoordinates) {
        header = coordinates
        measure()
    }

    fun onList(coordinates: LayoutCoordinates) {
        list = coordinates
        measure()
    }

    private fun measure() {
        val frame = frame?.takeIf { it.isAttached } ?: return
        header?.takeIf { it.isAttached }?.let { headerBottom = frame.localBoundingBoxOf(it, clipBounds = false).bottom }
        list?.takeIf { it.isAttached }?.let { listTop = frame.localPositionOf(it, Offset.Zero).y }
    }
}
