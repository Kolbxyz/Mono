package com.wander.mono.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HapticsTest {
    private val oreo = Build.VERSION_CODES.O
    private val android11 = Build.VERSION_CODES.R
    private val android14 = Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    @Test
    fun `ticks use the segment patterns from Android 14 and a clock tick before`() {
        assertEquals(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK, HapticCue.Tick.feedbackConstant(android14))
        assertEquals(HapticFeedbackConstants.SEGMENT_TICK, HapticCue.Segment.feedbackConstant(android14))
        assertEquals(HapticFeedbackConstants.CLOCK_TICK, HapticCue.Tick.feedbackConstant(android11))
        assertEquals(HapticFeedbackConstants.CLOCK_TICK, HapticCue.Segment.feedbackConstant(android11))
    }

    @Test
    fun `confirm and reject use their own patterns from Android 11`() {
        assertEquals(HapticFeedbackConstants.CONFIRM, HapticCue.Confirm.feedbackConstant(android11))
        assertEquals(HapticFeedbackConstants.REJECT, HapticCue.Reject.feedbackConstant(android11))
        assertEquals(HapticFeedbackConstants.VIRTUAL_KEY, HapticCue.Confirm.feedbackConstant(oreo))
        assertEquals(HapticFeedbackConstants.LONG_PRESS, HapticCue.Reject.feedbackConstant(oreo))
    }

    @Test
    fun `a gesture end before Android 11 stays silent`() {
        assertEquals(HapticFeedbackConstants.GESTURE_END, HapticCue.GestureEnd.feedbackConstant(android11))
        assertNull(HapticCue.GestureEnd.feedbackConstant(oreo))
    }

    @Test
    fun `thresholds fall back to a context click and long press is the same everywhere`() {
        assertEquals(
            HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE,
            HapticCue.Threshold.feedbackConstant(android14),
        )
        assertEquals(HapticFeedbackConstants.CONTEXT_CLICK, HapticCue.Threshold.feedbackConstant(android11))
        assertEquals(HapticFeedbackConstants.LONG_PRESS, HapticCue.LongPress.feedbackConstant(oreo))
        assertEquals(HapticFeedbackConstants.LONG_PRESS, HapticCue.LongPress.feedbackConstant(android14))
    }
}
