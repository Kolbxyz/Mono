package com.wander.mono

import android.app.Activity
import android.content.ActivityNotFoundException
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.widgets.WidgetHost
import com.wander.mono.core.widgets.WidgetRequest
import com.wander.mono.core.widgets.WidgetSetup
import com.wander.mono.ui.UserMessages
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * The activity side of hosted widgets: the host listens while home is started, and the bind
 * consent and configure steps of [WidgetSetup] run here and report back. The configure activity
 * starts through `AppWidgetHost`, which only delivers to `onActivityResult`; MainActivity forwards it.
 */
class WidgetActivityResults @Inject constructor(
    private val host: WidgetHost,
    private val setup: WidgetSetup,
    private val messages: UserMessages,
) : DefaultLifecycleObserver {
    /** Call from `onCreate`, after `super.onCreate`. */
    fun attach(activity: ComponentActivity) {
        val bind = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            setup.onBindResult(it.resultCode == Activity.RESULT_OK)
        }
        activity.lifecycle.addObserver(this)
        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                setup.requests.collect { request ->
                    when (request) {
                        is WidgetRequest.Bind -> start(isNew = true) { bind.launch(request.intent) }
                        is WidgetRequest.Configure -> start(request.isNew) {
                            val code = if (request.isNew) REQUEST_CONFIGURE else REQUEST_RECONFIGURE
                            host.startConfigure(activity, request.appWidgetId, code)
                        }
                    }
                }
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        host.startListening()
        // Providers uninstalled while home was stopped sent no event to a host not listening.
        setup.prune()
    }

    override fun onStop(owner: LifecycleOwner) = host.stopListening()

    /** True when [requestCode] was a widget's configure step. */
    fun onActivityResult(requestCode: Int, resultCode: Int): Boolean = when (requestCode) {
        REQUEST_CONFIGURE -> {
            setup.onConfigureResult(resultCode == Activity.RESULT_OK)
            true
        }
        REQUEST_RECONFIGURE -> true
        else -> false
    }

    private inline fun start(isNew: Boolean, launch: () -> Unit) {
        try {
            launch()
        } catch (_: ActivityNotFoundException) {
            failed(isNew)
        } catch (_: SecurityException) {
            failed(isNew)
        }
    }

    private fun failed(isNew: Boolean) {
        if (isNew) setup.cancel()
        messages.report(ActionResult.Failed(R.string.error_not_allowed))
    }

    private companion object {
        // Outside the range androidx's ActivityResultRegistry draws its own codes from (above 0xFFFF).
        const val REQUEST_CONFIGURE = 0x5701
        const val REQUEST_RECONFIGURE = 0x5702
    }
}
