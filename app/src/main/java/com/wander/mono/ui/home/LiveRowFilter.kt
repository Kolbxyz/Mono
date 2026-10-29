package com.wander.mono.ui.home

import com.wander.mono.core.media.MediaState
import com.wander.mono.data.LiveRowKind
import com.wander.mono.data.LiveRowRef

/** [this], or [MediaState.Idle] when the user switched the now-playing row off for the playing app. */
internal fun MediaState.unlessHidden(hidden: Set<LiveRowRef>): MediaState {
    if (this !is MediaState.Active) return this
    val off = hidden.any { it.kind == LiveRowKind.NowPlaying && it.app.packageName == media.packageName }
    return if (off) MediaState.Idle else this
}
