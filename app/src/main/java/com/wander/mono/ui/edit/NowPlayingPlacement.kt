package com.wander.mono.ui.edit

import com.wander.mono.data.NowPlayingAnchor
import com.wander.mono.ui.home.HomeRow

/**
 * [rows] as edit mode lays them out, with the now-playing spot always among them: in the card's
 * place while something plays, else where [anchor] puts it (first, while it follows its app or its
 * favourite is gone), so it can be moved even when nothing plays.
 */
internal fun withNowPlayingSpot(rows: List<HomeRow>, anchor: NowPlayingAnchor?): List<HomeRow> {
    if (rows.any { it is HomeRow.NowPlaying }) {
        return rows.map { if (it is HomeRow.NowPlaying) HomeRow.NowPlayingSpot else it }
    }
    val under = (anchor as? NowPlayingAnchor.Under)?.app
    val favorite = rows.indexOfFirst { it is HomeRow.App && it.app.key == under }
    return rows.toMutableList().apply { add(favorite + 1, HomeRow.NowPlayingSpot) }
}
