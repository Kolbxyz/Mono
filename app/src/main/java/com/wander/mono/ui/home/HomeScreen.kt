package com.wander.mono.ui.home

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.rememberOverscrollSwipe

/**
 * The home screen over the wallpaper: the glance header at the top, the rows anchored to the
 * bottom so favourites sit in the thumb zone, and the drawer button under them. Swiping up
 * anywhere (past the end of the list when it scrolls) opens the drawer. Long-pressing the clock or
 * an empty spot opens the home options; rows consume their own presses, so they never reach it.
 */
@Composable
fun HomeScreen(
    rows: List<HomeRow>?,
    glance: Glance,
    look: HomeLook,
    listState: LazyListState,
    onLaunch: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (positionMs: Long) -> Unit,
    onPlayQueueItem: (id: Long) -> Unit,
    onOpenOutput: () -> Unit,
    onGrantAccess: () -> Unit,
    onOpenDrawer: () -> Unit,
    onHomeOptions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val swipeUp = rememberOverscrollSwipe(upward = true, onSwipe = onOpenDrawer)
    // The header and drawer button are not scrollable; this lets drags on them reach swipeUp too.
    val stillDrag = rememberScrollableState { 0f }
    val motion = MaterialTheme.motionScheme
    val scheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val headerBottom = remember { mutableFloatStateOf(0f) }
    val listViewportTop = remember { mutableFloatStateOf(0f) }
    val scrimAlpha = if (look.textShadow) {
        0f
    } else {
        remember(look.tone, scheme.surface, scheme.onSurface) {
            scrimAlpha(look.tone, scheme.surface.toArgb(), scheme.onSurface.toArgb())
        }
    }
    val shadow = if (look.textShadow) {
        with(density) {
            Shadow(scheme.scrim.copy(alpha = SHADOW_ALPHA), Offset(0f, 1.dp.toPx()), blurRadius = 4.dp.toPx())
        }
    } else {
        null
    }

    // Without onSurface as the content colour, the clock and favourites would fall back to black.
    CompositionLocalProvider(LocalContentColor provides scheme.onSurface, LocalWallpaperTextShadow provides shadow) {
        BoxWithConstraints(
            modifier
                .fillMaxSize()
                .homeScrim(
                    color = scheme.surface.copy(alpha = scrimAlpha),
                    headerBottom = { headerBottom.floatValue },
                    listTop = { listViewportTop.floatValue + listState.contentTop() },
                    fade = with(density) { SCRIM_FADE.toPx() },
                )
                .pointerInput(onHomeOptions) { detectTapGestures(onLongPress = { onHomeOptions() }) }
                .nestedScroll(swipeUp)
                .scrollable(stillDrag, Orientation.Vertical),
            contentAlignment = Alignment.TopCenter,
        ) {
            val gutter = if (maxWidth >= MEDIUM_WIDTH) 32.dp else 24.dp
            Column(
                Modifier
                    .widthIn(max = MAX_COLUMN_WIDTH)
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = gutter),
            ) {
                val optionsLabel = stringResource(R.string.action_home_options)
                GlanceHeader(
                    glance,
                    Modifier
                        .onGloballyPositioned { headerBottom.floatValue = it.boundsInRoot().bottom }
                        .semantics {
                            onLongClick(optionsLabel) {
                                onHomeOptions()
                                true
                            }
                        },
                )
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .onGloballyPositioned { listViewportTop.floatValue = it.positionInRoot().y },
                    verticalArrangement = Arrangement.Bottom,
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    // The now-playing and prompt rows are not favourites: the empty state shows until there is one.
                    if (rows != null && rows.none { it is HomeRow.App }) {
                        item(key = "empty", contentType = "empty") {
                            EmptyHome(onChooseFavorites = onOpenDrawer, modifier = Modifier.animateItem())
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
                                onSeek = onSeek,
                                onPlayQueueItem = onPlayQueueItem,
                                onOpenOutput = onOpenOutput,
                                modifier = animated,
                            )
                            HomeRow.ListenerPrompt -> ListenerPromptRow(onGrantAccess, animated)
                        }
                    }
                }
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        painterResource(R.drawable.sym_apps),
                        contentDescription = stringResource(R.string.action_open_drawer),
                    )
                }
            }
        }
    }
}

/** Where the first row starts inside the list viewport: 0 once the list is scrolled or full. */
private fun LazyListState.contentTop(): Float {
    val first = layoutInfo.visibleItemsInfo.firstOrNull() ?: return 0f
    return if (first.index == 0) (first.offset - layoutInfo.viewportStartOffset).toFloat().coerceAtLeast(0f) else 0f
}

/** Text shadow strength, per spec 5.4: the `scrim` role at 40 %. */
private const val SHADOW_ALPHA = 0.4f

/** How far the scrim fades out into the empty space between header and list. */
private val SCRIM_FADE = 48.dp

/** Width from which the side gutter grows from 24 to 32 dp (M3 medium window class). */
private val MEDIUM_WIDTH = 600.dp

/** The column never spans wider than this; on tablets and unfolded phones it is centred. */
private val MAX_COLUMN_WIDTH = 560.dp
