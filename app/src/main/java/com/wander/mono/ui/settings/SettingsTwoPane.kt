package com.wander.mono.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.ui.components.SegmentGap
import com.wander.mono.ui.components.groupedListItem
import com.wander.mono.ui.components.readableColumn

/**
 * Settings on a wide window: the hub's categories down the left, the chosen one's page beside
 * them, so moving between pages is one tap and never a back. Back closes settings ([onBack]); the
 * first category is open to start with.
 */
@Composable
fun SettingsTwoPane(onBack: () -> Unit, settings: SettingsViewModel = viewModel()) {
    var page by rememberSaveable { mutableStateOf(SettingsCategory.entries.first()) }
    // Opened from the drawer's search field, the keyboard would otherwise stay up over the list.
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(keyboard) { keyboard?.hide() }
    val motion = MaterialTheme.motionScheme
    Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        SettingsPage(R.string.settings_title, onBack, Modifier.width(HubWidth).fillMaxHeight()) {
            item(key = "hub") {
                // The same segmented card as the single-pane hub, the open page's strip filled.
                val categories = SettingsCategory.entries
                Column(
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp).selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(SegmentGap),
                ) {
                    categories.forEachIndexed { index, category ->
                        val selected = category == page
                        val strip by animateColorAsState(
                            if (selected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            },
                            motion.defaultEffectsSpec(),
                        )
                        SettingsCategoryRow(
                            category,
                            onClick = { page = category },
                            modifier = Modifier
                                .groupedListItem(index, categories.size, strip)
                                .semantics { this.selected = selected },
                        )
                    }
                }
            }
        }
        AnimatedContent(
            targetState = page,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            transitionSpec = {
                (slideInVertically(motion.defaultSpatialSpec()) { it / SLIDE } + fadeIn(motion.defaultEffectsSpec()))
                    .togetherWith(fadeOut(motion.fastEffectsSpec()))
            },
            label = "settingsPane",
        ) { shown ->
            PagePane(shown, settings)
        }
    }
}

/** One category's page as the right pane: a collapsing title without a back arrow, its sections under it. */
@Composable
private fun PagePane(category: SettingsCategory, settings: SettingsViewModel) {
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scroll.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(category.title)) },
                scrollBehavior = scroll,
                // The pane meets the window's top and end edges, never its start.
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.End),
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
    ) { padding ->
        val sections = sectionsOf(category)
        LazyColumn(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.End))
                .readableColumn(),
            contentPadding = padding,
        ) {
            // A section's place on its page is its identity: the list is fixed per category.
            items(sections.size, key = { "${category.name}:$it" }) { sections[it](settings) }
        }
    }
}

/** The categories' column: wide enough for a title and its one-line summary. */
private val HubWidth = 360.dp

/** The page slides up from this fraction of its height. */
private const val SLIDE = 8
