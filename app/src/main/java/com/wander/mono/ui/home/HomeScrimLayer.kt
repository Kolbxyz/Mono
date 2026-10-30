package com.wander.mono.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity

/**
 * Home's legibility scrim and edit-mode dim, as a layer of their own under home: they never scale
 * with it, so as home shrinks back behind an overlay no scrim edge shows. Laid out the same size as
 * home's frame, where [edges] are measured (unscaled), with [listState] for where the rows start.
 * The scrim's edges taper in only while the window is unfocused ([windowFocusStrength]).
 */
@Composable
fun HomeScrimLayer(
    look: HomeLook,
    shell: HomeShell,
    editing: Boolean,
    edges: ScrimEdges,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val legibleAlpha = remember(look.tone, scheme.surface, scheme.onSurface) {
        scrimAlpha(look.tone, scheme.surface.toArgb(), scheme.onSurface.toArgb())
    }
    val scrimAlpha = homeScrimAlpha(legibleAlpha, look.textShadow, look.highContrast)
    // Edit mode dims the wallpaper (spec 4.6).
    val editDim = animateFloatAsState(if (editing) EDIT_DIM else 0f, MaterialTheme.motionScheme.defaultEffectsSpec())
    val strength = windowFocusStrength()
    val density = LocalDensity.current
    val reach = with(density) { ScrimReach.toPx() }
    val taper = with(density) { ScrimTaper.toPx() }
    val reachable = shell.reachable
    Spacer(
        modifier
            .fillMaxSize()
            .homeScrim(
                color = scheme.surface.copy(alpha = scrimAlpha),
                strength = { strength.value },
                // Reachable, the header is under the list: one scrim rising from the rows covers both.
                headerBottom = { if (reachable) -reach else edges.headerBottom },
                listTop = { edges.listTop + listState.contentTop() },
                reach = reach,
                taper = { taper * (1f - strength.value) },
            )
            .drawBehind { drawRect(scheme.surface.copy(alpha = editDim.value)) },
    )
}

/** How much edit mode dims the wallpaper, with the surface colour (spec 4.6). */
private const val EDIT_DIM = 0.6f
