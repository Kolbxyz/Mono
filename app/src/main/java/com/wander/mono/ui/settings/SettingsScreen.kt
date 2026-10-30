package com.wander.mono.ui.settings

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.ui.components.SegmentedColumn
import com.wander.mono.ui.components.backLean
import com.wander.mono.ui.components.backMorph
import com.wander.mono.ui.components.depthBlur
import com.wander.mono.ui.overlayScrim
import com.wander.mono.ui.theme.LocalReducedMotion
import kotlin.coroutines.cancellation.CancellationException

/**
 * Mono's own settings: a hub of [SettingsCategory] rows, each opening its page of sections. The
 * page is state inside this overlay, not a route, laid over the hub, which stays composed under
 * it (its scroll kept): the page opens out of a rounded card as the hub steps back, dims and
 * blurs; back (predictive, the page shrinking into that card and leaning off the swiped edge as
 * the hub comes forward) returns to the hub, and back on the hub closes settings via [onBack].
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    settings: SettingsViewModel = viewModel(),
) {
    var page by rememberSaveable { mutableStateOf<SettingsCategory?>(null) }
    // The page on screen: the open one, or the one still leaving after back.
    var shown by remember { mutableStateOf(page) }
    val scope = rememberCoroutineScope()
    val back = remember(scope) { PageBack(open = page != null, scope) }
    // Opened from the drawer's search field, the keyboard would otherwise stay up over the list.
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(keyboard) { keyboard?.hide() }
    val motion = MaterialTheme.motionScheme
    val enterSpec = motion.defaultSpatialSpec<Float>()
    val exitSpec = motion.fastSpatialSpec<Float>()
    val open = page != null
    LaunchedEffect(open) { back.follow(open, if (open) enterSpec else exitSpec) }
    PredictiveBackHandler(enabled = open) { progress ->
        try {
            progress.collect { back.swipe(it.progress, backLean(it.swipeEdge)) }
            page = null
        } catch (_: CancellationException) {
            // The swipe was abandoned: the page stays and grows back. A commit leaves the
            // progress alone, so the outgoing page carries on from its shrunk card.
            back.cancelled(exitSpec)
        }
    }
    val pageComposed by remember { derivedStateOf { page != null || back.presence != 0f } }
    // The blur is motion of its own: with motion reduced the hub only dims and steps back.
    val blur = !LocalReducedMotion.current
    val scrim = overlayScrim()
    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        SettingsPage(
            R.string.settings_title,
            onBack,
            Modifier
                // Under a whole page the hub is hidden: no blur pass for nothing.
                .drawWithContent { if (!back.covers) drawContent() }
                .depthBlur({ back.depth }, scrim, blur)
                .then(if (open) Modifier.clearAndSetSemantics {} else Modifier),
        ) {
            item(key = "hub") {
                SegmentedColumn(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingsCategory.entries.forEach { category ->
                        SettingsCategoryRow(category, onClick = {
                            shown = category
                            page = category
                        })
                    }
                }
            }
        }
        val showing = shown
        if (pageComposed && showing != null) {
            // Each category is its own page: a new one never inherits the last one's scroll.
            key(showing) {
                SettingsPage(
                    showing.title,
                    onBack = { page = null },
                    modifier = Modifier.backMorph({ back.morph }, { back.lean }, fade = { back.presence }),
                ) {
                    categoryPage(showing, settings)
                }
            }
        }
    }
}
