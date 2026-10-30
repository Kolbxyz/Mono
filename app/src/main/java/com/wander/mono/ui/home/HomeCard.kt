package com.wander.mono.ui.home

import androidx.compose.ui.unit.dp

/**
 * The one look home's cards share, so now playing, live activities, the calendar and widgets read
 * as siblings: `shapes.large` corners ([HomeCardCorner], the radius a press morphs from), content
 * [HomeCardInset] in from every edge, and [HomeCardMargin] above and below between rows.
 */
internal val HomeCardCorner = 26.dp

/** The card's inner padding on every edge. */
internal val HomeCardInset = 16.dp

/** The space a card keeps above and below it, on the 4 dp grid. */
internal val HomeCardMargin = 4.dp
