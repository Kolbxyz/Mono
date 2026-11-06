package com.wander.mono.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.drawer.DrawerScreen
import com.wander.mono.ui.drawer.DrawerViewModel
import com.wander.mono.ui.drawer.HitOutcome
import com.wander.mono.ui.drawer.PrivateSpaceActions
import com.wander.mono.ui.drawer.RailLanding
import com.wander.mono.ui.drawer.SearchHit
import com.wander.mono.ui.edit.EditHomeViewModel
import com.wander.mono.ui.home.HomeButtonViewModel
import com.wander.mono.ui.quiet.QuietDrawerGate
import com.wander.mono.ui.sheet.AppSheetViewModel

/**
 * The drawer's content wired to the launcher, the same whether it rises over home or stands beside
 * it as the wide layout's side panel: launching closes it, picking for home adds the app (or makes
 * it what home's button opens), Mono's own settings open over it. [open] and [revealed] are the
 * drawer's. [unlockHidden] is hoisted by the caller, so the screen lock's answer still has somewhere
 * to arrive if the drawer has left.
 * [landing] is the letter a scrub on home's edge let go on, for the drawer to open at.
 */
@Composable
fun LauncherDrawer(
    open: Boolean,
    revealed: () -> Float,
    quiet: Boolean,
    launcher: LauncherViewModel,
    drawer: DrawerViewModel,
    sheet: AppSheetViewModel,
    edit: EditHomeViewModel,
    unlockHidden: () -> Unit,
    modifier: Modifier = Modifier,
    landing: RailLanding? = null,
    homeButton: HomeButtonViewModel = viewModel(),
) {
    val query by drawer.query.collectAsStateWithLifecycle()
    val state by drawer.state.collectAsStateWithLifecycle()
    val recentSearches by drawer.recentSearches.collectAsStateWithLifecycle()
    val focusSearch by launcher.focusSearch.collectAsStateWithLifecycle()
    val picking by launcher.picking.collectAsStateWithLifecycle()
    val pickingApp = picking != null
    val buttonChosen by homeButton.chosen.collectAsStateWithLifecycle()
    val privateActions = remember(drawer) { PrivateSpaceActions(drawer::setPrivateLocked, drawer::openPrivateSettings) }
    val pick: (LaunchableApp) -> Unit = { app ->
        when (picking) {
            PickTarget.HomeButton -> homeButton.choose(app.key)
            PickTarget.Favorite, null -> edit.add(app.key)
        }
        launcher.closeDrawer()
    }
    val afterHit: (HitOutcome) -> Unit = { outcome ->
        when (outcome) {
            HitOutcome.Opened -> launcher.closeDrawer()
            HitOutcome.OpenMonoSettings -> launcher.openSettings()
            HitOutcome.Stayed -> Unit
        }
    }
    val onLaunchHit: (SearchHit) -> Unit = { hit ->
        if (pickingApp && hit is SearchHit.App) pick(hit.entry.app) else afterHit(drawer.launch(hit))
    }
    QuietDrawerGate(quiet = quiet, revealed = revealed, onClose = launcher::closeDrawer) {
        DrawerScreen(
            open = open,
            listActions = drawer.listActions,
            revealed = revealed,
            query = query,
            state = state,
            loadIcon = drawer::icon,
            peekIcon = drawer::cachedIcon,
            loadShortcutIcon = drawer::shortcutIcon,
            onQueryChange = drawer::setQuery,
            onSubmit = { drawer.withTopResult(pickingApp, onLaunchHit) },
            onLaunchApp = { if (pickingApp) pick(it) else if (drawer.launch(it)) launcher.closeDrawer() },
            onLaunchHit = onLaunchHit,
            onLongPress = { sheet.show(it.key) },
            onOpenSettings = launcher::openSettings,
            onUnlockHidden = unlockHidden,
            privateActions = privateActions,
            onSetWorkPaused = drawer::setWorkPaused,
            recentSearches = recentSearches,
            onForgetRecent = drawer::forgetRecent,
            onReach = { hit, how -> afterHit(drawer.reach(hit, how)) },
            modifier = modifier,
            pickingApp = pickingApp,
            pickHint = if (picking == PickTarget.HomeButton) {
                R.string.edit_pick_button_hint
            } else {
                R.string.edit_pick_hint
            },
            // Picking for the button, once it opens an app, it can go back to search.
            onUseSearch = if (picking == PickTarget.HomeButton && buttonChosen) {
                {
                    homeButton.useSearch()
                    launcher.closeDrawer()
                }
            } else {
                null
            },
            focusSearch = focusSearch,
            landing = landing,
        )
    }
}
