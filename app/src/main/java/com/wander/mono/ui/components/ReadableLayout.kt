package com.wander.mono.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spec 5.2 in numbers: the side gutter by M3 width class, the readable column that tablets and
 * unfolded phones centre, the compact height class (a phone in landscape) where home's header
 * gives the list its room, and the expanded width class where home and the drawer sit side by side.
 */
object ReadableLayout {
    /** Rows never span wider than this; wider windows centre the column. */
    val MaxColumnWidth = 560.dp

    /** Start of the M3 medium width class. */
    val MediumWidth = 600.dp

    /** Start of the M3 expanded width class. */
    val ExpandedWidth = 840.dp

    /** End of the M3 compact height class: below it, a phone is on its side. */
    val CompactHeight = 480.dp

    /** 36 dp on compact widths, 48 dp from medium on: home's clock and rows sit well in from the edge. */
    fun gutter(width: Dp): Dp = if (width >= MediumWidth) 48.dp else 36.dp

    fun isCompactHeight(height: Dp): Boolean = height < CompactHeight

    /**
     * Room for two panes: an expanded width that is not just a phone on its side, which is as wide
     * but too short for two lists and keeps the single column.
     */
    fun isWide(width: Dp, height: Dp): Boolean = width >= ExpandedWidth && !isCompactHeight(height)
}
