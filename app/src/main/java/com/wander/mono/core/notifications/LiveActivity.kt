package com.wander.mono.core.notifications

import android.app.PendingIntent
import androidx.compose.ui.graphics.ImageBitmap

/** Which live row an ongoing notification becomes. */
enum class LiveActivityKind {
    /** Something under way with a progress: a download, a delivery, an Android 16 Live Update. */
    Progress,

    /** A running (or paused) timer or stopwatch, mostly the clock app's. */
    Timer,

    /** Turn-by-turn directions: the next turn, its distance and the street. */
    Navigation,
}

/**
 * An ongoing notification shown as a live row under its app. Only notifications whose content may
 * show in full become one, so [title] and [text] are as posted.
 */
data class LiveActivity(
    /** `StatusBarNotification.key`. */
    val key: String,
    val owner: PackageUser,
    val kind: LiveActivityKind,
    val title: String?,
    val text: String?,
    /** Null when the notification shows no progress bar. */
    val progress: LiveProgress?,
    /** Null when the notification shows no running chronometer (a paused timer, most rows). */
    val chronometer: Chronometer?,
    /** The next turn's arrow, for navigation only. */
    val icon: LiveIcon?,
    /** The notification's own plain buttons ("Pause", "Stop"), in its order. */
    val actions: List<PreviewAction>,
    /** What tapping it in the shade opens; null when the app gave nothing. */
    val contentIntent: PendingIntent?,
)

/** A notification's progress bar. */
sealed interface LiveProgress {
    /** [fraction] is 0 to 1. */
    data class Determinate(val fraction: Float) : LiveProgress

    data object Indeterminate : LiveProgress
}

/**
 * The notification's chronometer: counting up from [base], or down to it when [countsDown]. [base]
 * is the notification's `when`, on the wall clock.
 */
data class Chronometer(val base: Long, val countsDown: Boolean)

/** [image] is a full-colour large icon, or the app's small icon when [tintable] (an alpha mask to tint). */
data class LiveIcon(val image: ImageBitmap, val tintable: Boolean)
