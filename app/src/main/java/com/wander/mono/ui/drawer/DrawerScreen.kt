package com.wander.mono.ui.drawer

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.wander.mono.ui.theme.LocalHighContrast

/**
 * All apps, alphabetical (or newest first), with the letter rail, the list's floating toolbar and
 * category chips, and the work profile and private space at the end; or, while a query is typed, the
 * ranked results, the two cross-fading. The search field docks at the bottom, over the navigation
 * bar and riding up with the keyboard; the list runs from the top down to it and the best result
 * sits right on it. While the empty field has focus, [recentSearches] show above it as chips;
 * contacts in the results offer [onReach]'s actions. Drawn on the background the user chose, an even
 * veil that stays put and fades in with the drawer while only the content rises, its blur following
 * how far the drawer is [revealed]. With [focusSearch] (opened from home's search button, the search
 * tile or a search gesture), the search field takes focus and the keyboard opens; a plain swipe never
 * does: the field rests folded into a round button at the bottom end, and a tap grows it back out.
 * Once no longer [open], or gone, the keyboard goes. Dragging it closed is handled by its container.
 * A scrub of home's rail let go on a letter ([landing]) opens it at that letter.
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
    @StringRes pickHint: Int = R.string.edit_pick_hint,
    onUseSearch: (() -> Unit)? = null,
    focusSearch: Boolean = false,
    landing: RailLanding? = null,
) {
    val icons = remember(state.iconMode, state.iconPack, loadIcon) {
        AppIcons(state.iconMode, loadIcon, state.iconPack)
    }
    // With the keyboard: the dock rides up on it, and results rest on the dock.
    val bottom = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
    val topAndSides = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    val searchFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    // The field folds into a round button at rest; opened for search, or tapped, it grows out of it.
    var searchOpen by rememberSaveable { mutableStateOf(focusSearch) }
    var focusAsked by remember { mutableStateOf(false) }
    LaunchedEffect(open, focusSearch) {
        if (!open) {
            // Closing: the keyboard leaves with the drawer instead of staying up over home.
            keyboard?.hide()
            focusManager.clearFocus()
            searchOpen = false
        } else if (focusSearch) {
            searchOpen = true
            focusAsked = true
        }
    }
    LaunchedEffect(focusAsked) {
        if (focusAsked) {
            // The field can only take focus once it is laid out, a frame after it first composes.
            withFrameNanos { }
            searchFocus.requestFocus()
            keyboard?.show()
            focusAsked = false
        }
    }
    // Leaving while Mono is not on screen (an app launched from search) skips the frame above: the
    // keyboard must not come back over home, nor over the next opening, which may be a plain swipe.
    DisposableEffect(keyboard) { onDispose { keyboard?.hide() } }
    // Taken once, as this opening first composes: a scrub from home lands the list on its letter.
    var landed by remember {
        mutableIntStateOf(landing?.take()?.takeIf { it in 1..state.sections.size } ?: RailLanding.NONE)
    }
    // Hoisted, so the alphabetical list keeps its place through a search.
    val appListState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (landed > 0) RailEntries.listItemFor(landed, state.railLead) else 0,
    )
    val shown = rememberShownResults(query, state)
    val density = LocalDensity.current
    // The dock's own height, keyboard and navigation bar aside: what the lists keep clear of.
    var dock by remember { mutableStateOf(0.dp) }
    val lift = LocalDrawerLift.current
    // Over the wallpaper (bare, veiled or blurred) as over the surface, text takes the surface's
    // content colour; on the bare wallpaper it gets a shadow too, as home's does.
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onSurface,
        LocalShortcutIcons provides loadShortcutIcon,
        LocalWallpaperTextShadow provides drawerTextShadow(state.look.background),
        LocalHighContrast provides state.look.highContrast,
    ) {
        Column(
            modifier
                .fillMaxSize()
                .steadyBackdrop(drawerBackdrop(state.look, revealed), lift)
                .windowInsetsPadding(topAndSides)
                .readableColumn(),
        ) {
            if (pickingApp) PickHint(pickHint, onUseSearch)
            Box(Modifier.weight(1f).fillMaxWidth()) {
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
                            dock,
                            appListState,
                            landed = landed,
                            onLanded = { landed = RailLanding.NONE },
                        )
                    } else {
                        // Ends on the dock rather than running under it: nothing scrolls behind the field.
                        val clear = bottom.asPaddingValues().calculateBottomPadding() + dock
                        SearchResultList(
                            results = if (pickingApp) pickable(shown.results) else shown.results,
                            settled = shown.settled,
                            icons = icons,
                            onLaunchHit = onLaunchHit,
                            onLongPress = onLongPress,
                            contentPadding = PaddingValues(top = 16.dp, bottom = 4.dp),
                            modifier = Modifier.padding(bottom = clear),
                            onUnlockHidden = onUnlockHidden.takeIf { state.hiddenLocked },
                            onReach = onReach,
                        )
                    }
                }
                SearchField(
                    query,
                    onQueryChange,
                    onSubmit,
                    onOpenSettings,
                    searchFocus,
                    collapsed = !searchOpen && query.isEmpty(),
                    onExpand = {
                        searchOpen = true
                        focusAsked = true
                    },
                    // Left empty, the field folds back into its button.
                    onBlur = { if (query.isEmpty()) searchOpen = false },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .windowInsetsPadding(bottom)
                        .onSizeChanged { dock = with(density) { it.height.toDp() } }
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
                    recent = recentSearches,
                    onForgetRecent = onForgetRecent,
                )
            }
        }
    }
}
