package com.wander.mono.ui.drawer

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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.apps.WorkProfile
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.readableColumn

/**
 * All apps, alphabetical, with the letter rail and the work profile and private space at the bottom;
 * or, while a query is typed, the ranked results. Drawn on the background the user chose. With
 * [focusSearch] (opened from home's search bar) the search field takes focus and the keyboard opens.
 * Dragging it closed is handled by its container.
 */
@Composable
fun DrawerScreen(
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
    onSetWorkPaused: (WorkProfile, Boolean) -> Unit,
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
    LaunchedEffect(focusSearch) {
        if (!focusSearch) return@LaunchedEffect
        // The field can only take focus once it is laid out, a frame after the drawer first composes.
        withFrameNanos { }
        searchFocus.requestFocus()
        keyboard?.show()
    }
    // Over the wallpaper (scrim or blur) as over the surface, text takes the surface's content colour.
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onSurface,
        LocalShortcutIcons provides loadShortcutIcon,
    ) {
        Column(
            modifier
                .fillMaxSize()
                .background(drawerBackdrop(state.look))
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
            )
            if (pickingApp) {
                Text(
                    stringResource(R.string.edit_pick_hint),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                )
            }
            if (query.isBlank()) {
                // Picking for home leaves the private space out: its apps never go on home.
                val actions = privateActions.takeUnless { pickingApp }
                DrawerAppList(state, icons, actions, onSetWorkPaused, onLaunchApp, onLongPress, bottom)
            } else {
                SearchResultList(
                    results = if (pickingApp) pickable(state.results) else state.results,
                    icons = icons,
                    onLaunchHit = onLaunchHit,
                    onLongPress = onLongPress,
                    contentPadding = bottom,
                    onUnlockHidden = onUnlockHidden.takeIf { state.hiddenLocked },
                )
            }
        }
    }
}
