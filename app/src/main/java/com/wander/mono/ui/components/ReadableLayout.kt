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

    /** 24 dp on compact widths, 32 dp from medium on. */
    fun gutter(width: Dp): Dp = if (width >= MediumWidth) 32.dp else 24.dp

    fun isCompactHeight(height: Dp): Boolean = height < CompactHeight

    /**
     * Room for two panes: an expanded width that is not just a phone on its side, which is as wide
     * but too short for two lists and keeps the single column.
     */
    fun isWide(width: Dp, height: Dp): Boolean = width >= ExpandedWidth && !isCompactHeight(height)
}
