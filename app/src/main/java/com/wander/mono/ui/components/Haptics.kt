package com.wander.mono.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Mono's haptic vocabulary: one word per kind of moment, each mapped once to the platform's
 * constant for it, with the closest older constant (or nothing) where the SDK lacks it. Through
 * [View.performHapticFeedback], so the system's "touch feedback" setting is honoured without a
 * check here, the same as Compose's `LocalHapticFeedback`, whose types resolve to these constants.
 *
 * What each word is for, and who does it today (the sweep moves them here):
 * - [tick]: scrubbing through many values, e.g. letters on the drawer rail (`LetterRail`).
 * - [segment]: a discrete step, e.g. a reorder slot crossed (`ui/edit`), seek bar intervals.
 * - [threshold]: a gesture armed past its point of no return (`DrawerMotion`, `SearchPull`,
 *   `SkipSwipe`, reorder lift).
 * - [confirm]: an action done, e.g. a media skip or an up-next pick (`MediaTransport`, `UpNextList`).
 * - [reject]: an action failed (`LauncherSnackbar` errors).
 * - [gestureEnd]: a drag released and settled (drawer settle, reorder drop, seek release).
 * - [longPress]: a long press that opened something (home's sheet, reorder hold, widgets).
 */
@Stable
class MonoHaptics internal constructor(private val view: View) {
    fun tick() = perform(HapticCue.Tick)

    fun segment() = perform(HapticCue.Segment)

    fun threshold() = perform(HapticCue.Threshold)

    fun confirm() = perform(HapticCue.Confirm)

    fun reject() = perform(HapticCue.Reject)

    fun gestureEnd() = perform(HapticCue.GestureEnd)

    fun longPress() = perform(HapticCue.LongPress)

    private fun perform(cue: HapticCue) {
        cue.feedbackConstant(Build.VERSION.SDK_INT)?.let { view.performHapticFeedback(it) }
    }
}

/** The [MonoHaptics] for this composition's view. */
@Composable
fun rememberMonoHaptics(): MonoHaptics {
    val view = LocalView.current
    return remember(view) { MonoHaptics(view) }
}

/** The moments [MonoHaptics] has words for. */
internal enum class HapticCue { Tick, Segment, Threshold, Confirm, Reject, GestureEnd, LongPress }

/**
 * The [HapticFeedbackConstants] value for this cue on [sdk], or null where the SDK has no fitting
 * one: a gesture end before Android 11 stays silent rather than buzzing like something else.
 */
internal fun HapticCue.feedbackConstant(sdk: Int): Int? = when (this) {
    HapticCue.Tick ->
        if (sdk >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
        } else {
            HapticFeedbackConstants.CLOCK_TICK
        }
    HapticCue.Segment ->
        if (sdk >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.SEGMENT_TICK
        } else {
            HapticFeedbackConstants.CLOCK_TICK
        }
    HapticCue.Threshold ->
        if (sdk >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE
        } else {
            HapticFeedbackConstants.CONTEXT_CLICK
        }
    HapticCue.Confirm ->
        if (sdk >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY
    // Before Android 11 the heavier long-press buzz is the one pattern that reads as "no".
    HapticCue.Reject ->
        if (sdk >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS
    HapticCue.GestureEnd -> if (sdk >= Build.VERSION_CODES.R) HapticFeedbackConstants.GESTURE_END else null
    HapticCue.LongPress -> HapticFeedbackConstants.LONG_PRESS
}
