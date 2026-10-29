package com.wander.mono

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wander.mono.core.apps.ShortcutRepository
import com.wander.mono.core.calendar.CalendarAccess
import com.wander.mono.core.notifications.ListenerAccess
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.data.settings.themeChoice
import com.wander.mono.ui.LauncherApp
import com.wander.mono.ui.LauncherViewModel
import com.wander.mono.ui.onboarding.Onboarding
import com.wander.mono.ui.sheet.AppSheetViewModel
import com.wander.mono.ui.theme.MonoTheme
import com.wander.mono.ui.theme.isDark
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var shortcuts: ShortcutRepository

    @Inject
    lateinit var listenerAccess: ListenerAccess

    @Inject
    lateinit var settings: SettingsStore

    @Inject
    lateinit var calendarAccess: CalendarAccess

    private val launcher: LauncherViewModel by viewModels()
    private val sheet: AppSheetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Nothing is drawn until the theme is read (a few ms, once per process): drawing the
            // default first would flash the wrong theme on every cold start.
            val theme = remember { settings.themeChoice() }.collectAsStateWithLifecycle(null).value
                ?: return@setContent
            val dark = theme.mode.isDark()
            // The bar icons follow Mono's theme, not the system's, when the user has fixed one.
            LaunchedEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { dark },
                )
            }
            MonoTheme(darkTheme = dark, amoledBlack = theme.amoledBlack) {
                Box {
                    LauncherApp()
                    Onboarding()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Neither the default-home choice nor the notification-access grant sends an event; the
        // user changes both in Settings and comes back here, so this is the moment to look.
        shortcuts.recheckHostPermission()
        listenerAccess.recheck()
        calendarAccess.recheck()
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

    private companion object {
        // androidx.activity's own navigation bar scrims, which only show on 3-button navigation.
        val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
