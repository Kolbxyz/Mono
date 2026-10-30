package com.wander.mono.ui.settings

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.ui.components.SegmentedColumn
import com.wander.mono.ui.components.backResistance
import kotlin.coroutines.cancellation.CancellationException

/**
 * Mono's own settings: a hub of [SettingsCategory] rows, each opening its page of sections. The
 * page is state inside this overlay, not a route: back (predictive, the page shrinks toward the
 * edge as the swipe goes) returns to the hub, and back on the hub closes settings via [onBack].
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    settings: SettingsViewModel = viewModel(),
) {
    var page by rememberSaveable { mutableStateOf<SettingsCategory?>(null) }
    val scope = rememberCoroutineScope()
    val back = remember(scope) { PageBack(scope) }
    // Opened from the drawer's search field, the keyboard would otherwise stay up over the list.
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(keyboard) { keyboard?.hide() }
    val motion = MaterialTheme.motionScheme
    val cancelSpec = motion.fastSpatialSpec<Float>()
    PredictiveBackHandler(enabled = page != null) { progress ->
        try {
            progress.collect { back.swipe(it.progress) }
            page = null
        } catch (_: CancellationException) {
            // The swipe was abandoned: the page stays and grows back. A commit leaves the
            // progress alone, so the outgoing page fades out at its shrunk size.
            back.cancelled(cancelSpec)
        }
    }
    AnimatedContent(
        targetState = page,
        modifier = modifier,
        transitionSpec = {
            val forward = targetState != null
            (slideInHorizontally(motion.defaultSpatialSpec()) { if (forward) it / SLIDE else -it / SLIDE } +
                fadeIn(motion.defaultEffectsSpec()))
                .togetherWith(fadeOut(motion.fastEffectsSpec()))
        },
        label = "settingsPage",
    ) { shown ->
        if (shown == null) {
            SettingsPage(R.string.settings_title, onBack) {
                item(key = "hub") {
                    SegmentedColumn(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        SettingsCategory.entries.forEach { category ->
                            SettingsCategoryRow(category, onClick = {
                                back.reset()
                                page = category
                            })
                        }
                    }
                }
            }
        } else {
            SettingsPage(
                shown.title,
                onBack = { page = null },
                modifier = Modifier.graphicsLayer {
                    val scale = lerp(1f, BACK_MIN_SCALE, backResistance(back.progress))
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
                },
            ) {
                val sections = sectionsOf(shown)
                // A section's place on its page is its identity: the list is fixed per category.
                items(sections.size, key = { "${shown.name}:$it" }) { sections[it](settings) }
            }
        }
    }
}

/** How far a page has shrunk at the far end of a back swipe, before it commits. */
private const val BACK_MIN_SCALE = 0.9f

/** Pages slide in from this fraction of the width. */
private const val SLIDE = 4
