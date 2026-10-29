package com.wander.mono.core.system

import android.accessibilityservice.AccessibilityService
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Locking the screen and pulling down the notification shade from home, which only an
 * accessibility service may do. [GestureService] binds itself here while the user has it switched
 * on; until then each action fails with a message instead of doing nothing.
 */
@Singleton
class GestureActions @Inject constructor(@ApplicationContext private val context: Context) {
    /** Runs one of [AccessibilityService]'s global actions; set while [GestureService] is connected. */
    private var perform: ((Int) -> Boolean)? = null

    private val mutableAvailable = MutableStateFlow(false)

    /** Whether the user has switched Mono's gesture service on in Android's accessibility settings. */
    val isAvailable: StateFlow<Boolean> = mutableAvailable.asStateFlow()

    internal fun bind(globalAction: ((Int) -> Boolean)?) {
        perform = globalAction
        mutableAvailable.value = globalAction != null
    }

    fun lockScreen(): ActionResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return ActionResult.Failed(R.string.error_lock_unsupported)
        return run(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
    }

    fun openNotifications(): ActionResult = run(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)

    /** Android's accessibility page, where the user switches the gesture service on once. */
    fun openAccessSettings(): ActionResult = try {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        ActionResult.Done
    } catch (_: ActivityNotFoundException) {
        ActionResult.Failed(R.string.error_settings_missing)
    }

    private fun run(action: Int): ActionResult {
        val globalAction = perform ?: return ActionResult.Failed(R.string.error_gesture_service_off)
        return if (globalAction(action)) ActionResult.Done else ActionResult.Failed(R.string.error_gesture_refused)
    }
}
