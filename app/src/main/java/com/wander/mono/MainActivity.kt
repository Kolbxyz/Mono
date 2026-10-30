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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wander.mono.core.apps.PrivateSpace
import com.wander.mono.core.apps.ShortcutRepository
import com.wander.mono.core.calendar.CalendarAccess
import com.wander.mono.core.notifications.ListenerAccess
import com.wander.mono.core.tiles.TileRequest
import com.wander.mono.core.widgets.WidgetHost
import com.wander.mono.ui.LauncherApp
import com.wander.mono.ui.LauncherViewModel
import com.wander.mono.ui.drawer.DrawerViewModel
import com.wander.mono.ui.home.LocalTopResumed
import com.wander.mono.ui.onboarding.Onboarding
import com.wander.mono.ui.sheet.AppSheetViewModel
import com.wander.mono.ui.theme.MonoTheme
import com.wander.mono.ui.theme.isDark
import com.wander.mono.ui.theme.showWallpaper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var shortcuts: ShortcutRepository

    @Inject
    lateinit var listenerAccess: ListenerAccess

    @Inject
    lateinit var widgetHost: WidgetHost

    @Inject
    lateinit var widgetResults: WidgetActivityResults

    @Inject
    lateinit var calendarAccess: CalendarAccess

    @Inject
    lateinit var privateSpace: PrivateSpace

    private val launcher: LauncherViewModel by viewModels()
    private val sheet: AppSheetViewModel by viewModels()
    private val themes: ThemeViewModel by viewModels()
    private val drawer: DrawerViewModel by viewModels()

    /** A Quick Settings tile's request, run once Mono is in front. */
    private var tileRequest: TileRequest? = null

    /** Whether Mono is the top resumed activity; home's scrim starts stepping aside the moment it is not. */
    private val topResumed = mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) tileRequest = TileRequest.of(intent)
        widgetResults.attach(this)
        setContent {
            // Nothing is drawn until the theme is read (a few ms, once per process): drawing the
            // default first would flash the wrong theme on every cold start. A recreated activity
            // finds it already read in the view model and draws from its first frame.
            val theme = themes.theme.collectAsStateWithLifecycle().value ?: return@setContent
            val dark = theme.mode.isDark()
            val blackWindow = dark && theme.amoledBlack && theme.blackWallpaper
            // The bar icons follow Mono's theme, not the system's, when the user has fixed one; set
            // in the same frame as the content, not one after it.
            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { dark },
                )
                window.showWallpaper(!blackWindow)
            }
            MonoTheme(
                darkTheme = dark,
                amoledBlack = theme.amoledBlack,
                iconShape = theme.iconShape,
                iconTheming = theme.iconTheming,
            ) {
                CompositionLocalProvider(LocalTopResumed provides topResumed.value) {
                    Box {
                        LauncherApp()
                        Onboarding()
                    }
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
        // The private space can finish unlocking after its last broadcast, leaving it listed empty.
        privateSpace.recheck()
    }

    override fun onResume() {
        super.onResume()
        when (tileRequest ?: return) {
            TileRequest.Search -> launcher.openDrawer(focusSearch = true)
            TileRequest.LockPrivateSpace -> drawer.setPrivateLocked(true)
            TileRequest.UnlockPrivateSpace -> {
                // Into the drawer, where the private space's apps appear once it unlocks.
                launcher.openDrawer()
                drawer.setPrivateLocked(false)
            }
        }
        tileRequest = null
    }

    // Android 10 and up. Called as another activity starts to take over (the recents gesture
    // included), before the window loses focus, so the scrim's fade starts earlier.
    override fun onTopResumedActivityChanged(isTopResumedActivity: Boolean) {
        super.onTopResumedActivityChanged(isTopResumedActivity)
        topResumed.value = isTopResumedActivity
    }

    override fun onStop() {
        super.onStop()
        // A recreation (locale, font scale) keeps the drawer; really leaving resets to home.
        if (!isChangingConfigurations) launcher.leaveToHome()
    }

    override fun onDestroy() {
        super.onDestroy()
        // The kept widget views hold this activity as their context.
        widgetHost.releaseViews()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        TileRequest.of(intent)?.let {
            sheet.dismiss()
            tileRequest = it
            return
        }
        if (intent.action != Intent.ACTION_MAIN || !intent.hasCategory(Intent.CATEGORY_HOME)) return
        // Launcher3's test: focused and not just brought back from another app.
        val alreadyOnHome = hasWindowFocus() && intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT == 0
        sheet.dismiss()
        launcher.onHomePressed(alreadyOnHome)
    }

    @Deprecated("Widget configure activities only report here; see WidgetActivityResults.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!widgetResults.onActivityResult(requestCode, resultCode)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private companion object {
        // androidx.activity's own navigation bar scrims, which only show on 3-button navigation.
        val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
