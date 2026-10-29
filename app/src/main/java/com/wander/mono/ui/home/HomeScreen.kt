package com.wander.mono.ui.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.rememberOverscrollSwipe

/**
 * The home list over the wallpaper: glance header, then the rows. Swiping up past the end of the
 * list (or the button at the bottom) opens the drawer. Long-pressing the clock or an empty spot
 * opens the home options; rows consume their own presses, so they never reach the list's detector.
 */
@Composable
fun HomeScreen(
    rows: List<HomeRow>?,
    glance: Glance,
    listState: LazyListState,
    onLaunch: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onGrantAccess: () -> Unit,
    onOpenDrawer: () -> Unit,
    onHomeOptions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val swipeUp = rememberOverscrollSwipe(upward = true, onSwipe = onOpenDrawer)
    val motion = MaterialTheme.motionScheme
    val scheme = MaterialTheme.colorScheme
    // A Surface, not a bare background: it also sets LocalContentColor to onSurface, which the
    // clock, the favourites and the drawer button inherit. Without it they fell back to black.
    Surface(
        modifier.fillMaxSize(),
        color = scheme.surface.copy(alpha = WALLPAPER_SCRIM),
        contentColor = scheme.onSurface,
    ) {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(onHomeOptions) { detectTapGestures(onLongPress = { onHomeOptions() }) }
                    .nestedScroll(swipeUp),
                contentPadding = WindowInsets.safeDrawing
                    .add(WindowInsets(left = 24.dp, right = 24.dp, bottom = BOTTOM_CLEARANCE))
                    .asPaddingValues(),
            ) {
                item(key = "glance", contentType = "glance") {
                    val label = stringResource(R.string.action_home_options)
                    GlanceHeader(
                        glance,
                        Modifier.semantics {
                            onLongClick(label) {
                                onHomeOptions()
                                true
                            }
                        },
                    )
                }
                // The now-playing and prompt rows are not favourites: the hint shows until there is one.
                if (rows != null && rows.none { it is HomeRow.App }) {
                    item(key = "empty", contentType = "empty") {
                        Text(
                            stringResource(R.string.home_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
                items(rows.orEmpty(), key = { it.key }, contentType = { it::class }) { row ->
                    val animated = Modifier.animateItem(
                        fadeInSpec = motion.defaultEffectsSpec(),
                        placementSpec = motion.defaultSpatialSpec(),
                        fadeOutSpec = motion.fastEffectsSpec(),
                    )
                    when (row) {
                        is HomeRow.App -> FavoriteRow(
                            row = row,
                            onLaunch = { onLaunch(row.app) },
                            onLongPress = { onLongPress(row.app) },
                            modifier = animated,
                        )
                        is HomeRow.NowPlaying -> NowPlayingCard(
                            media = row.media,
                            onTogglePlay = onTogglePlay,
                            onSkipNext = onSkipNext,
                            onSkipPrevious = onSkipPrevious,
                            modifier = animated,
                        )
                        HomeRow.ListenerPrompt -> ListenerPromptRow(onGrantAccess, animated)
                        is NotificationRow -> NotificationPreviewRow(row, animated)
                    }
                }
            }
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(bottom = 8.dp),
            ) {
                Icon(
                    painterResource(R.drawable.sym_apps),
                    contentDescription = stringResource(R.string.action_open_drawer),
                )
            }
        }
    }
}

/**
 * Keeps the wallpaper visible while making M3 text colours readable on any wallpaper: at 0.7 the
 * surface scrim keeps onSurfaceVariant above 4.5:1 even over a wallpaper of the opposite brightness.
 */
private const val WALLPAPER_SCRIM = 0.7f

/** Room under the last row for the drawer button. */
private val BOTTOM_CLEARANCE = 72.dp
