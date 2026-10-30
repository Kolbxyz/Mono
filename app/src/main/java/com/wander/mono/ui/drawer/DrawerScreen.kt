package com.wander.mono.ui.drawer

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.apps.WorkProfile
import com.wander.mono.core.contacts.ContactReach
import com.wander.mono.data.RecentSearch
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.readableColumn
import com.wander.mono.ui.home.LocalWallpaperTextShadow
import com.wander.mono.ui.home.onWallpaper
import com.wander.mono.ui.theme.LocalHighContrast

/**
 * All apps, alphabetical (or newest first), with the letter rail, the list's floating toolbar and
 * category chips, and the work profile and private space at the bottom;
 * or, while a query is typed, the ranked results, the two cross-fading. While the empty field has
 * focus, [recentSearches] show under it as chips; contacts in the results offer [onReach]'s actions.
 * Drawn on the background the user chose, its blur following how far the drawer is [revealed]. With
 * [focusSearch] (opened from home's search bar or a search gesture), or on every opening when the
 * user asked for the keyboard on open, the search field takes focus and the keyboard opens; so does
 * a second swipe up the list. Once no longer [open], the keyboard goes. Dragging it closed is handled
 * by its container.
 */
@Composable
fun DrawerScreen(
    open: Boolean,
    revealed: () -> Float,
    query: String,
    state: DrawerUiState,
    loadIcon: suspend (AppKey, IconMode, Int) -> AppIconImage?,
    loadShortcutIcon: ShortcutIconLoader,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLaunchHit: (SearchHit) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    onOpenSettings: () -> Unit,
    onUnlockHidden: () -> Unit,
    privateActions: PrivateSpaceActions,
    listActions: DrawerListActions,
    onSetWorkPaused: (WorkProfile, Boolean) -> Unit,
    recentSearches: List<RecentSearch>,
    onForgetRecent: (RecentSearch) -> Unit,
    onReach: (SearchHit.Person, ContactReach) -> Unit,
    modifier: Modifier = Modifier,
    pickingApp: Boolean = false,
    focusSearch: Boolean = false,
) {
    val icons = remember(state.iconMode, state.iconPack, loadIcon) {
        AppIcons(state.iconMode, loadIcon, state.iconPack)
    }
    val bottom = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues()
    val topAndSides = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    val searchFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusOnOpen = focusSearch || state.keyboardOnOpen
    LaunchedEffect(open, focusOnOpen) {
        if (!open) {
            // Closing: the keyboard leaves with the drawer instead of staying up over home.
            keyboard?.hide()
            focusManager.clearFocus()
        } else if (focusOnOpen) {
            // The field can only take focus once it is laid out, a frame after the drawer first composes.
            withFrameNanos { }
            searchFocus.requestFocus()
            keyboard?.show()
        }
    }
    // Hoisted, so the alphabetical list keeps its place through a search.
    val appListState = rememberLazyListState()
    val shown = rememberShownResults(query, state)
    // Over the wallpaper (bare, scrim or blur) as over the surface, text takes the surface's content
    // colour; on the wallpaper it gets a shadow too, as home's does.
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onSurface,
        LocalShortcutIcons provides loadShortcutIcon,
        LocalWallpaperTextShadow provides drawerTextShadow(state.look.background),
        LocalHighContrast provides state.look.highContrast,
    ) {
        Column(
            modifier
                .fillMaxSize()
                .background(drawerBackdrop(state.look, revealed))
                .windowInsetsPadding(topAndSides)
                .readableColumn(),
        ) {
            SearchField(
                query,
                onQueryChange,
                onSubmit,
                onOpenSettings,
                searchFocus,
                Modifier.fillMaxWidth().padding(16.dp),
                recent = recentSearches,
                onForgetRecent = onForgetRecent,
            )
            if (pickingApp) {
                Text(
                    stringResource(R.string.edit_pick_hint),
                    style = MaterialTheme.typography.titleMedium.onWallpaper(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                )
            }
            Crossfade(
                targetState = query.isBlank(),
                animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                label = "drawerContent",
            ) { browsing ->
                if (browsing) {
                    // Picking for home leaves the private space out: its apps never go on home.
                    val actions = privateActions.takeUnless { pickingApp }
                    DrawerAppList(
                        state,
                        icons,
                        actions,
                        listActions,
                        onSetWorkPaused,
                        onLaunchApp,
                        onLongPress,
                        bottom,
                        appListState,
                        onPullUp = {
                            searchFocus.requestFocus()
                            keyboard?.show()
                        },
                    )
                } else {
                    SearchResultList(
                        results = if (pickingApp) pickable(shown.results) else shown.results,
                        settled = shown.settled,
                        icons = icons,
                        onLaunchHit = onLaunchHit,
                        onLongPress = onLongPress,
                        contentPadding = bottom,
                        onUnlockHidden = onUnlockHidden.takeIf { state.hiddenLocked },
                        onReach = onReach,
                    )
                }
            }
        }
    }
}
