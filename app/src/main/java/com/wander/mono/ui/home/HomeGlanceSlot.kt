package com.wander.mono.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import com.wander.mono.R

/**
 * The glance header in home's column, above the list or, [shell] reachable, below it. A long press
 * on the clock opens its sheet ([onClockOptions]); elsewhere on the header it is home's own (home
 * options), so TalkBack gets that here as a named action. [onPlaced] reports where it sits, for
 * the scrim.
 */
@Composable
internal fun HomeGlanceSlot(
    glance: Glance,
    shell: HomeShell,
    compact: Boolean,
    hero: () -> Float,
    onPlaced: (LayoutCoordinates) -> Unit,
    onHomeOptions: () -> Unit,
    onClockOptions: () -> Unit,
) {
    val optionsLabel = stringResource(R.string.action_home_options)
    GlanceHeader(
        glance,
        compact = compact,
        reachable = shell.reachable,
        simple = shell.simple,
        hero = hero,
        onClockOptions = onClockOptions,
        modifier = Modifier
            .onGloballyPositioned(onPlaced)
            .semantics {
                onLongClick(optionsLabel) {
                    onHomeOptions()
                    true
                }
            },
    )
}
