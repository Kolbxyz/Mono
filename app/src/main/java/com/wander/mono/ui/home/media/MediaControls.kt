package com.wander.mono.ui.home.media

/** What the player under the clock can ask of the playing session. */
interface MediaControls {
    fun togglePlayPause()

    fun skipNext()

    fun skipPrevious()

    fun seekTo(positionMs: Long)

    fun playQueueItem(id: Long)

    fun openOutputSwitcher()

    /** Opens the playing app, at its own player when the session names one. */
    fun openPlayer()
}
