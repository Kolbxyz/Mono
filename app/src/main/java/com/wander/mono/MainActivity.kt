package com.wander.mono

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.wander.mono.core.apps.ShortcutRepository
import com.wander.mono.core.notifications.ListenerAccess
import com.wander.mono.ui.LauncherApp
import com.wander.mono.ui.LauncherViewModel
import com.wander.mono.ui.sheet.AppSheetViewModel
import com.wander.mono.ui.theme.MonoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var shortcuts: ShortcutRepository

    @Inject
    lateinit var listenerAccess: ListenerAccess

    private val launcher: LauncherViewModel by viewModels()
    private val sheet: AppSheetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MonoTheme { LauncherApp() } }
    }

    override fun onStart() {
        super.onStart()
        // Neither the default-home choice nor the notification-access grant sends an event; the
        // user changes both in Settings and comes back here, so this is the moment to look.
        shortcuts.recheckHostPermission()
        listenerAccess.recheck()
    }

    override fun onStop() {
        super.onStop()
        // A rotation recreates the activity and keeps the drawer; really leaving resets to home.
        if (!isChangingConfigurations) launcher.leaveToHome()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action != Intent.ACTION_MAIN || !intent.hasCategory(Intent.CATEGORY_HOME)) return
        // Launcher3's test: focused and not just brought back from another app.
        val alreadyOnHome = hasWindowFocus() && intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT == 0
        sheet.dismiss()
        launcher.onHomePressed(alreadyOnHome)
    }
}
