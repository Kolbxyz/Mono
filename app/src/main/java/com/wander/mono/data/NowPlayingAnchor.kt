package com.wander.mono.data

import com.wander.mono.core.apps.AppKey

/**
 * Where the user placed the now-playing card in edit mode: above the first favourite ([Top]) or
 * right under one ([Under]). Unplaced (no anchor stored), it sits under the app that plays.
 */
sealed interface NowPlayingAnchor {
    data object Top : NowPlayingAnchor

    data class Under(val app: AppKey) : NowPlayingAnchor

    fun encode(): String = when (this) {
        Top -> TOP
        is Under -> app.encode()
    }

    companion object {
        fun decode(encoded: String): NowPlayingAnchor? =
            if (encoded == TOP) Top else AppKey.decode(encoded)?.let { Under(it) }
    }
}

/** [NowPlayingAnchor.Top] as stored; an encoded AppKey always has two slashes, so it never reads as this. */
private const val TOP = "top"
