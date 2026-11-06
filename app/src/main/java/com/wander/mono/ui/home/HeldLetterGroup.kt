package com.wander.mono.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.readableColumn
import com.wander.mono.ui.drawer.AppGroup
import com.wander.mono.ui.drawer.AppGroupItem
import com.wander.mono.ui.drawer.DrawerUiState
import com.wander.mono.ui.drawer.RAIL_WIDTH
import com.wander.mono.ui.drawer.RailEntries
import com.wander.mono.ui.drawer.drawerTextShadow
import com.wander.mono.ui.drawer.entranceMotion
import com.wander.mono.ui.drawer.rememberListEntrance

/**
 * The letter a finger holds on home's rail ([entry], 1 the first letter), drawn where the drawer's
 * list would show it at the top of the screen and exactly as the drawer draws it ([AppGroupItem]),
 * so lifting the finger into the drawer moves nothing. Compose it while a finger is on the rail: the
 * letter shown as it lands has its rows rise in one after another; from then on each new letter
 * slides in from the way the finger went (from below going down the alphabet, from above going up)
 * as the last one slides away the other way. ★ and no finger ([entry] below 1) show nothing: home
 * itself is back.
 */
@Composable
internal fun HeldLetterGroup(
    entry: Int,
    drawer: DrawerUiState,
    icons: AppIcons,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val letters = drawer.sections.size
    val item = RailEntries.listItemFor(entry, drawer.railLead)
    val group = if (entry in 1..letters) drawer.groups.getOrNull(item) else null
    val motion = MaterialTheme.motionScheme
    val shift = with(LocalDensity.current) { LETTER_SHIFT.roundToPx() }
    // As the drawer's list: its rail's lane on the rail's side, a plain margin on the other.
    val start = if (drawer.railAtStart) RAIL_WIDTH else 16.dp
    val end = if (drawer.railAtStart) 16.dp else RAIL_WIDTH
    // Composed as the finger lands: the letters shown in the first moment rise in row by row,
    // later ones only slide.
    val entrance = rememberListEntrance()
    val topAndSides = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onSurface,
        LocalWallpaperTextShadow provides drawerTextShadow(drawer.look.background),
    ) {
        AnimatedContent(
            targetState = HeldLetter(entry, group),
            modifier = modifier
                .fillMaxSize()
                .clipToBounds()
                .windowInsetsPadding(topAndSides)
                .readableColumn()
                .padding(start = start, end = end),
            transitionSpec = {
                val down = if (targetState.entry >= initialState.entry) 1 else -1
                val enter = slideInVertically(motion.fastSpatialSpec()) { shift * down } +
                    fadeIn(motion.fastEffectsSpec())
                val exit = slideOutVertically(motion.fastSpatialSpec()) { -shift * down } +
                    fadeOut(motion.fastEffectsSpec())
                enter togetherWith exit
            },
            contentKey = { it.group?.key },
            label = "heldLetter",
        ) { held ->
            Box(Modifier.fillMaxWidth()) {
                val shown = held.group ?: return@Box
                AppGroupItem(
                    shown,
                    icons,
                    onLaunchApp,
                    onLongPress,
                    rowEntrance = { row -> entranceMotion(entrance, row) },
                )
            }
        }
    }
}

/** What home shows for a held rail entry: its group, or none for ★ and for no finger. */
private data class HeldLetter(val entry: Int, val group: AppGroup?)

/** How far a letter's apps slide as the next letter takes their place. */
private val LETTER_SHIFT = 48.dp
