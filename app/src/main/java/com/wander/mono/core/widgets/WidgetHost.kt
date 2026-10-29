package com.wander.mono.core.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * The process's one `AppWidgetHost`. It listens for widget updates only while home is started
 * (the activity calls [startListening] / [stopListening]); updates sent meanwhile arrive on start.
 */
@Singleton
class WidgetHost @Inject constructor(@ApplicationContext context: Context) {
    private val manager = AppWidgetManager.getInstance(context)
    private val mutableProvidersChanged = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** A widget provider was installed, updated or removed; bound widgets may have died with it. */
    val providersChanged: SharedFlow<Unit> = mutableProvidersChanged.asSharedFlow()

    private val host = object : AppWidgetHost(context, HOST_ID) {
        override fun onCreateView(context: Context, appWidgetId: Int, appWidget: AppWidgetProviderInfo?) =
            MonoWidgetHostView(context)

        override fun onProvidersChanged() {
            mutableProvidersChanged.tryEmit(Unit)
        }
    }

    fun startListening() = host.startListening()

    fun stopListening() = host.stopListening()

    fun allocate(): Int = host.allocateAppWidgetId()

    fun delete(appWidgetId: Int) = host.deleteAppWidgetId(appWidgetId)

    /** Every id this host holds, placed on home or not. */
    fun allIds(): Set<Int> = host.appWidgetIds.toSet()

    /** Null once the widget is gone: its provider was uninstalled or the binding was dropped. */
    fun info(appWidgetId: Int): AppWidgetProviderInfo? = manager.getAppWidgetInfo(appWidgetId)

    /** A fresh view for a placed widget; [context] is the activity's, so it follows Mono's theme. */
    fun createView(context: Context, appWidgetId: Int): MonoWidgetHostView =
        host.createView(context, appWidgetId, info(appWidgetId)) as MonoWidgetHostView

    /**
     * A view that only shows a provider's preview layout (Android 12+), for the picker; never bound.
     * It takes no touches, so a tap anywhere on it picks the widget.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun createPreview(context: Context, info: AppWidgetProviderInfo): AppWidgetHostView =
        object : AppWidgetHostView(context) {
            override fun onInterceptTouchEvent(event: MotionEvent) = true

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouchEvent(event: MotionEvent) = false
        }.apply {
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            setAppWidget(NO_ID, info)
            updateAppWidget(RemoteViews(info.provider.packageName, info.previewLayout))
        }

    /** Runs through the system, so configure activities that are not exported, or in a work profile, start too. */
    fun startConfigure(activity: Activity, appWidgetId: Int, requestCode: Int) =
        host.startAppWidgetConfigureActivityForResult(activity, appWidgetId, 0, requestCode, null)

    private companion object {
        /** Any constant works: it only tells Mono's host apart from other hosts in the same app. */
        const val HOST_ID = 1024
        const val NO_ID = -1
    }
}
