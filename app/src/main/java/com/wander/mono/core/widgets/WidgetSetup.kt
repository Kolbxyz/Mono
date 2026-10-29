package com.wander.mono.core.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.Profiles
import com.wander.mono.di.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** A step of adding a widget that needs an activity: the system's bind consent or the provider's setup. */
sealed interface WidgetRequest {
    data class Bind(val intent: Intent) : WidgetRequest

    /** [isNew] is false when reconfiguring a widget already on home; its result changes nothing then. */
    data class Configure(val appWidgetId: Int, val isNew: Boolean) : WidgetRequest
}

/**
 * Adding a widget: allocate an id, bind it (silently when the user once allowed Mono to, else
 * through the system's consent screen), run the provider's configure activity when it has one,
 * and only then place it on home. Any step cancelled frees the id again.
 */
@Singleton
class WidgetSetup @Inject constructor(
    @ApplicationContext private val context: Context,
    private val host: WidgetHost,
    private val widgets: Widgets,
    private val profiles: Profiles,
    @AppScope private val scope: CoroutineScope,
) {
    private class Pending(val appWidgetId: Int, val info: AppWidgetProviderInfo, val anchor: AppKey?)

    private val manager = AppWidgetManager.getInstance(context)
    private val mutableRequests = Channel<WidgetRequest>(Channel.BUFFERED)
    private var pending: Pending? = null

    /** Delivered once each, to the started activity. */
    val requests: Flow<WidgetRequest> = mutableRequests.receiveAsFlow()

    init {
        host.providersChanged.onEach { prune() }.launchIn(scope)
    }

    fun add(info: AppWidgetProviderInfo, anchor: AppKey?) {
        cancel()
        val id = host.allocate()
        pending = Pending(id, info, anchor)
        if (manager.bindAppWidgetIdIfAllowed(id, info.profile, info.provider, null)) {
            afterBind()
        } else {
            val consent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, info.profile)
            mutableRequests.trySend(WidgetRequest.Bind(consent))
        }
    }

    fun reconfigure(appWidgetId: Int) {
        mutableRequests.trySend(WidgetRequest.Configure(appWidgetId, isNew = false))
    }

    fun onBindResult(granted: Boolean) = if (granted) afterBind() else cancel()

    fun onConfigureResult(done: Boolean) = if (done) place() else cancel()

    /** The pending setup could not go on (its activity failed to start): free its id. */
    fun cancel() {
        pending?.let { host.delete(it.appWidgetId) }
        pending = null
    }

    /** On start and whenever providers change: drop dead widgets, free leaked ids. */
    fun prune() {
        val keep = setOfNotNull(pending?.appWidgetId)
        scope.launch { widgets.prune(keep) }
    }

    private fun afterBind() {
        val current = pending ?: return
        val info = host.info(current.appWidgetId) ?: current.info
        if (info.needsConfigure()) {
            mutableRequests.trySend(WidgetRequest.Configure(current.appWidgetId, isNew = true))
        } else {
            place()
        }
    }

    private fun place() {
        val current = pending ?: return
        pending = null
        val density = context.resources.displayMetrics.density
        val placement = WidgetPlacement(
            appWidgetId = current.appWidgetId,
            provider = current.info.provider.flattenToString(),
            userSerial = profiles.serialOf(current.info.profile),
            anchor = current.anchor,
            heightDp = WidgetSize.fitting((current.info.minHeight / density).toInt()).heightDp,
        )
        scope.launch { widgets.add(placement) }
    }
}

/** A configure activity is required unless the provider marks it optional (Android 12+). */
internal fun AppWidgetProviderInfo.needsConfigure(): Boolean = configure != null &&
    (
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            widgetFeatures and AppWidgetProviderInfo.WIDGET_FEATURE_CONFIGURATION_OPTIONAL == 0
        )

/** Only providers that declare it may be configured again after placing (Android 9+). */
internal fun AppWidgetProviderInfo.isReconfigurable(): Boolean = configure != null &&
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
    widgetFeatures and AppWidgetProviderInfo.WIDGET_FEATURE_RECONFIGURABLE != 0
