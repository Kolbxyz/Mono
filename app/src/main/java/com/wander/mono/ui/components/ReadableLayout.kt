package com.wander.mono.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spec 5.2 in numbers: the side gutter by M3 width class, the readable column that tablets and
 * unfolded phones centre, and the compact height class (a phone in landscape) where home's
 * header gives the list its room.
 */
object ReadableLayout {
    /** Rows never span wider than this; wider windows centre the column. */
    val MaxColumnWidth = 560.dp

    /** Start of the M3 medium width class. */
    val MediumWidth = 600.dp

    /** End of the M3 compact height class: below it, a phone is on its side. */
    val CompactHeight = 480.dp

    /** 24 dp on compact widths, 32 dp from medium on. */
    fun gutter(width: Dp): Dp = if (width >= MediumWidth) 32.dp else 24.dp

    fun isCompactHeight(height: Dp): Boolean = height < CompactHeight
}
