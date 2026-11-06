package com.wander.mono.ui.drawer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.apps.PrivateSpaceState
import com.wander.mono.core.apps.WorkProfile
import com.wander.mono.data.settings.DrawerSort
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.theme.schemeAnimateItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The listed apps in their groups (new apps, then letters; or by install age), with the letter rail
 * in A–Z order, then a card per work profile and the private space at the end; no private space
 * without [privateActions] (while picking an app for home, where private apps never go).
 * Bare, as Niagara's: the list runs from the top to the bottom of the screen, under the search
 * [dock] (its height over the navigation bar), its last rows scrolling clear of it; its ends give like
 * rubber and bounce back ([BounceOverscroll]). The rail runs down to the dock ([DrawerRail]), and a
 * jump puts its letter at the top. Category chips, when turned on in the list options, unfold just
 * above the dock. [listState] is the caller's, so the list keeps its place while search results show
 * instead.
 * TalkBack gets a "Jump to" action per letter on the list itself. Rows rise in as the list first
 * shows, and its ends fade out while there is more to scroll to. While a finger holds a letter on the
 * rail, only that letter's apps stay; the rest fade out. Opened from a scrub on home that [landed] on a
 * letter, the list starts there as if that letter were still held, and the rest fade in around it once
 * the drawer is [revealed];
 * [onLanded] hears that it has, so the list shown again after a search starts as usual.
 */
@Composable
internal fun DrawerAppList(
    state: DrawerUiState,
    icons: AppIcons,
    privateActions: PrivateSpaceActions?,
    listActions: DrawerListActions,
    onSetWorkPaused: (WorkProfile, Boolean) -> Unit,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    dock: Dp,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    landed: Int = RailLanding.NONE,
    onLanded: () -> Unit = {},
    revealed: () -> Float = { 1f },
) {
    val scope = rememberCoroutineScope()
    val choices = state.controls.choices
    val lead = state.railLead
    val showRail = choices.sort == DrawerSort.Alphabetical && state.sections.isNotEmpty()
    // The private space is personal: the Work filter leaves it out, as picking for home does.
    val space = (state.privateSpace as? PrivateSpaceState.Present)?.takeIf { choices.profile != ProfileFilter.Work }
    val resources = LocalResources.current
    val jumpActions = remember(state.sections, lead, showRail, resources, listState) {
        if (!showRail) return@remember emptyList()
        state.sections.mapIndexed { i, section ->
            CustomAccessibilityAction(resources.getString(R.string.drawer_jump_to_letter, section.letter)) {
                scope.launch { listState.scrollToItem(i + lead) }
                true
            }
        }
    }
    // Landed from home, the held letter is already in place: no row makes an entrance.
    val entrance = rememberListEntrance(opening = landed == RailLanding.NONE)
    val hold = remember { RailHold().apply { entry = landed } }
    // Once the drawer has faded in, the landing lets go and the other letters fade in around it:
    // one fade after the other, never two at once.
    LaunchedEffect(hold) {
        if (hold.entry != RailLanding.NONE) {
            snapshotFlow { revealed() }.first { it >= 1f }
            hold.entry = RailLanding.NONE
            onLanded()
        }
    }
    val letters = state.sections.size
    val bounce = rememberBounceOverscroll()
    val atStart = state.railAtStart
    val sideStart = if (atStart && showRail) RAIL_WIDTH else 16.dp
    val sideEnd = if (!atStart && showRail) RAIL_WIDTH else 16.dp
    // Not the keyboard: the list and rail would re-measure, and the letters reflow, every frame it slides.
    val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val clear = navigationBottom + dock
    // Turned on, the chips sit over the list's foot: its last rows scroll clear of them too.
    val chips = if (choices.showCategories && state.controls.categories.isNotEmpty()) CHIPS_ROOM else 0.dp
    Box(modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .listFadingEdges(listState)
                // Inside the fading edges, which stay at the screen's edges while the rows bounce.
                .graphicsLayer { translationY = bounce.offset }
                .semantics { customActions = jumpActions },
            contentPadding = PaddingValues(start = sideStart, end = sideEnd, bottom = clear + chips + 8.dp),
            overscrollEffect = bounce,
        ) {
            appGroupItems(state.groups, icons, listState, entrance, onLaunchApp, onLongPress) { index ->
                hold.shows(index, lead, letters)
            }
            // After the groups, so the rail's indexes still match the list's.
            items(state.work, key = { "work:${it.profile.serial}" }) { section ->
                val shown = hold.heldGroup(lead, letters) < 0
                WorkSectionCard(
                    section,
                    icons,
                    onSetWorkPaused,
                    onLaunchApp,
                    onLongPress,
                    schemeAnimateItem().heldFade(shown),
                )
            }
            if (space != null && privateActions != null) {
                item(key = "private") {
                    PrivateSection(
                        space,
                        state.privateApps,
                        icons,
                        privateActions,
                        onLaunchApp,
                        onLongPress,
                        schemeAnimateItem().heldFade(hold.heldGroup(lead, letters) < 0),
                    )
                }
            }
        }
        if (showRail) DrawerRail(state, listState, space, privateActions, hold, bottom = clear)
        CategoryChips(
            visible = choices.showCategories,
            categories = state.controls.categories,
            selected = choices.category,
            onSelect = listActions.onCategory,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = clear),
        )
    }
}

/** The category chips' row, with its gap above the dock. */
private val CHIPS_ROOM = 56.dp
