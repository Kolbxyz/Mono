package com.wander.mono.core.widgets

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Outline
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewOutlineProvider
import kotlin.math.hypot

/**
 * A hosted widget's view. Taps go to the widget; a long press anywhere on it (even on a button)
 * is taken from the widget and reported through [onLongPress], as Launcher3 does. The corners are
 * clipped to [cornerRadius] so every widget shares the row's rounded shape.
 */
class MonoWidgetHostView(context: Context) : AppWidgetHostView(context) {
    var onLongPress: () -> Unit = {}

    var cornerRadius: Float = 0f
        set(value) {
            if (field == value) return
            field = value
            invalidateOutline()
        }

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var downX = 0f
    private var downY = 0f
    private var longPressed = false
    private var appliedSize: Pair<Int, Int>? = null
    private val longPress = Runnable {
        longPressed = true
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        onLongPress()
    }

    init {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
            }
        }
        clipToOutline = true
    }

    /** Once the long press fired, the widget's own children get a cancel and see nothing more. */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        track(event)
        return longPressed
    }

    /** Touches no child took still count towards the long press. */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        track(event)
        return true
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(longPress)
        super.onDetachedFromWindow()
    }

    /**
     * Tells the provider the size it is shown at, so it can pick its layout. Only sends when the
     * size changed: each update is a call into the provider's process.
     */
    fun applySize(widthDp: Int, heightDp: Int) {
        val size = widthDp to heightDp
        if (size == appliedSize) return
        appliedSize = size
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            updateAppWidgetSize(Bundle(), listOf(SizeF(widthDp.toFloat(), heightDp.toFloat())))
        } else {
            val options = Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp)
            }
            AppWidgetManager.getInstance(context).updateAppWidgetOptions(appWidgetId, options)
        }
    }

    private fun track(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                removeCallbacks(longPress)
                longPressed = false
                downX = event.x
                downY = event.y
                postDelayed(longPress, ViewConfiguration.getLongPressTimeout().toLong())
            }
            MotionEvent.ACTION_MOVE -> {
                if (hypot(event.x - downX, event.y - downY) > touchSlop) removeCallbacks(longPress)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> removeCallbacks(longPress)
        }
    }
}
