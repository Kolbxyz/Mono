package com.wander.mono.ui.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import kotlinx.coroutines.launch

/**
 * The listed apps in their groups (new apps, then letters; or by install age), with the letter rail
 * in A–Z order, then a card per work profile and the private space at the end; no private space
 * without [privateActions] (while picking an app for home, where private apps never go).
 * Built for one hand: the list rests low, its first rows just above the search [dock] (its height
 * over the navigation bar), with a lead of empty space above them that scrolls away; a short list
 * sits on the dock. The rail rests low on its edge too ([DrawerRail]); a jump lands its letter at
 * that same thumb height. Category chips unfold just above the dock, and a floating toolbar above
 * them sets the list up, getting out of the way as the list scrolls on. [listState] is the
 * caller's, so the list keeps its place while search results show instead.
 * TalkBack gets a "Jump to" action per letter on the list itself. Rows rise in as the list first
 * shows, and its ends fade out while there is more to scroll to; [railScrub] drives the rail from home.
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
    railScrub: RailScrub? = null,
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
    val entrance = rememberListEntrance()
    val toolbarScroll = rememberToolbarScroll()
    // Always there while the list cannot scroll back, so it is never lost on a short list.
    val toolbarShown by remember(listState, toolbarScroll) {
        derivedStateOf { toolbarScroll.visible || !listState.canScrollBackward }
    }
    val atStart = state.railAtStart
    val sideStart = if (atStart && showRail) RAIL_WIDTH else 16.dp
    val sideEnd = if (!atStart && showRail) RAIL_WIDTH else 16.dp
    // Not the keyboard: the list and rail would re-measure, and the letters reflow, every frame it slides.
    val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Column(modifier.fillMaxSize().padding(bottom = navigationBottom + dock)) {
        BoxWithConstraints(Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .listFadingEdges(listState)
                    .nestedScroll(toolbarScroll)
                    .semantics { customActions = jumpActions },
                contentPadding = PaddingValues(
                    start = sideStart,
                    end = sideEnd,
                    top = maxHeight * THUMB_LEAD,
                    bottom = DrawerToolbarClearance + 8.dp,
                ),
                verticalArrangement = Arrangement.Bottom,
            ) {
                appGroupItems(state.groups, icons, listState, entrance, onLaunchApp, onLongPress)
                // After the groups, so the rail's indexes still match the list's.
                items(state.work, key = { "work:${it.profile.serial}" }) { section ->
                    WorkSectionCard(section, icons, onSetWorkPaused, onLaunchApp, onLongPress, schemeAnimateItem())
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
                            schemeAnimateItem(),
                        )
                    }
                }
            }
            if (showRail) DrawerRail(state, listState, maxHeight, space, privateActions, railScrub)
            DrawerToolbar(
                visible = toolbarShown,
                controls = state.controls,
                actions = listActions,
                // Centred between the rail's lanes, so it never covers a letter.
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = RAIL_WIDTH),
            )
        }
        // In the thumb zone, under the toolbar that unfolds them, over the search field.
        CategoryChips(
            visible = choices.showCategories,
            categories = state.controls.categories,
            selected = choices.category,
            onSelect = listActions.onCategory,
        )
    }
}

/**
 * Share of the list's height left empty above its first row while it is scrolled to the top: the
 * first letters rest in the lower part of the screen, where the thumb is, instead of under the
 * status bar. Scrolling on takes the space away like any other.
 */
private const val THUMB_LEAD = 0.4f
