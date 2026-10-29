package com.wander.mono.core.system

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Whether Mono is the default home app, and the system screen that makes it so. Becoming default
 * sends no event, so callers re-check when the user comes back.
 */
@Singleton
class HomeRole @Inject constructor(@ApplicationContext private val context: Context) {
    fun isHeld(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.getSystemService(RoleManager::class.java).isRoleHeld(RoleManager.ROLE_HOME)
    } else {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        context.packageManager.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName == context.packageName
    }

    /** The system's own "make Mono default" dialog; before Android 10, the default-home settings page. */
    fun requestIntent(): Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.getSystemService(RoleManager::class.java).createRequestRoleIntent(RoleManager.ROLE_HOME)
    } else {
        Intent(Settings.ACTION_HOME_SETTINGS)
    }
}
