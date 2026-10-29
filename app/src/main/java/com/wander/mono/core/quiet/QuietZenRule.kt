package com.wander.mono.core.quiet

import android.app.AutomaticZenRule
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.notification.Condition
import androidx.annotation.RequiresApi
import com.wander.mono.MainActivity
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mono's own Android mode ("Mono quiet mode", Do Not Disturb with the user's priority settings),
 * switched on and off with quiet mode. Owning a rule, rather than setting the global interruption
 * filter, keeps the user's own modes untouched and shows Mono's in the system's Modes list.
 * Needs the Do Not Disturb access the user grants in Android's settings; without it nothing happens.
 */
@Singleton
class QuietZenRule @Inject constructor(@ApplicationContext private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    private val conditionId: Uri = Uri.Builder()
        .scheme(Condition.SCHEME)
        .authority(context.packageName)
        .appendPath(CONDITION_PATH)
        .build()

    /** Setting a rule's state from the app needs Android 10. */
    val isSupported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    fun isGranted(): Boolean = manager.isNotificationPolicyAccessGranted

    fun openAccessSettings(): ActionResult = try {
        context.startActivity(
            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        ActionResult.Done
    } catch (_: ActivityNotFoundException) {
        ActionResult.Failed(R.string.error_settings_missing)
    }

    /** Turns Mono's mode on or off; does nothing without access, which the user can revoke any time. */
    fun setActive(active: Boolean) {
        if (!isSupported || !isGranted()) return
        applyState(active)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun applyState(active: Boolean) {
        val state = if (active) Condition.STATE_TRUE else Condition.STATE_FALSE
        val summary = context.getString(R.string.quiet_zen_rule_name)
        manager.setAutomaticZenRuleState(ruleId(), Condition(conditionId, summary, state))
    }

    /** The rule Mono added before, found by its condition, or a new one. */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun ruleId(): String =
        manager.automaticZenRules.entries.firstOrNull { it.value.conditionId == conditionId }?.key
            ?: manager.addAutomaticZenRule(
                AutomaticZenRule(
                    context.getString(R.string.quiet_zen_rule_name),
                    null,
                    ComponentName(context, MainActivity::class.java),
                    conditionId,
                    null,
                    NotificationManager.INTERRUPTION_FILTER_PRIORITY,
                    true,
                ),
            )

    private companion object {
        const val CONDITION_PATH = "quiet"
    }
}
