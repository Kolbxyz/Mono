package com.wander.mono.ui.home.media

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.wander.mono.R
import com.wander.mono.core.media.PlayingMedia
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.home.HomePlayer
import com.wander.mono.ui.theme.textAlpha

/**
 * The player's first line, the same in both forms so it morphs rather than swaps: the artwork,
 * growing with [morph] from a thumb to a cover and rounding less as it grows; the words (one line
 * of title · artist collapsed; expanded, the app and output chips over title and artist); and,
 * collapsed only, the small transport, which folds away sideways. Tapping the artwork opens the
 * player app. [compact] keeps the cover and title short when home has little room.
 */
@Composable
internal fun PlayerHead(
    player: HomePlayer,
    morph: Float,
    expanded: Boolean,
    compact: Boolean,
    tint: Color,
    controls: MediaControls,
    icons: AppIcons,
) {
    val motion = MaterialTheme.motionScheme
    val grown = morph.coerceAtLeast(0f)
    // Eased, so the cover settles smaller when home runs short of room instead of jumping.
    val cover by animateDpAsState(
        if (compact) COMPACT_ART else EXPANDED_ART,
        motion.defaultSpatialSpec(),
        label = "cover",
    )
    val artSize = lerp(COLLAPSED_ART, cover, grown)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        PlayerArt(
            media = player.media,
            size = artSize,
            corner = lerp(COLLAPSED_ART / 2, artSize * EXPANDED_CORNER, grown.coerceAtMost(1f)),
            onOpen = controls::openPlayer,
        )
        Spacer(Modifier.width(lerp(COLLAPSED_GAP, EXPANDED_GAP, grown)))
        PlayerWords(player, expanded, compact, icons, controls, Modifier.weight(1f))
        AnimatedVisibility(
            visible = !expanded,
            enter = expandHorizontally(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
            exit = shrinkHorizontally(motion.defaultSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
        ) {
            CompactTransport(player.media, tint, controls, Modifier.padding(start = COLLAPSED_GAP))
        }
    }
}

/** The artwork, crossfading when the track changes; a tap opens the player app. */
@Composable
private fun PlayerArt(media: PlayingMedia, size: Dp, corner: Dp, onOpen: () -> Unit) {
    val effects = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    AnimatedContent(
        targetState = TrackArt.of(media),
        contentKey = { it.key },
        transitionSpec = { fadeIn(effects) togetherWith fadeOut(effects) },
        // A fixed size, so the container follows the morph frame by frame instead of easing after it.
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .clickable(onClickLabel = stringResource(R.string.media_open_player), onClick = onOpen),
        label = "track-art",
    ) { track -> MediaArt(track.art, size, corner = corner) }
}

/**
 * The words beside the artwork. Collapsed: title · artist on one line, ellipsized, never scrolling.
 * Expanded: one row with the app chip and where it is heard, then the title (one line when
 * [compact]) over the artist. They crossfade while the line resizes.
 */
@Composable
private fun PlayerWords(
    player: HomePlayer,
    expanded: Boolean,
    compact: Boolean,
    icons: AppIcons,
    controls: MediaControls,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = expanded,
        transitionSpec = {
            (fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()))
                .using(SizeTransform(clip = false) { _, _ -> motion.defaultSpatialSpec() })
        },
        contentAlignment = Alignment.CenterStart,
        modifier = modifier,
        label = "player-words",
    ) { full ->
        if (full) {
            Column {
                PlayerChips(player, icons, controls, Modifier.padding(bottom = CHIP_GAP))
                MediaTitle(player.media, large = true, centred = false, titleLines = if (compact) 1 else 2)
            }
        } else {
            StripLine(player.media)
        }
    }
}

/** Title · artist on one line, the artist dimmer, as the strip shows them. */
@Composable
private fun StripLine(media: PlayingMedia) {
    val artistColor = LocalContentColor.current.copy(alpha = textAlpha(ARTIST_ALPHA))
    val separator = stringResource(R.string.media_title_separator)
    val line = buildAnnotatedString {
        append(media.title)
        media.artist?.let { artist ->
            withStyle(SpanStyle(color = artistColor)) {
                append(separator)
                append(artist)
            }
        }
    }
    Text(line, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

private val COLLAPSED_ART = 44.dp
private val EXPANDED_ART = 96.dp

/** On a phone on its side, or when home is short of room, the cover stays small so the list keeps room. */
private val COMPACT_ART = 72.dp

/** Expanded artwork corners as a share of its size; collapsed, the thumb is round like the strip. */
private const val EXPANDED_CORNER = 0.2f

private val COLLAPSED_GAP = 12.dp
private val EXPANDED_GAP = 16.dp
private val CHIP_GAP = 6.dp

/** The artist, dimmer than the title in the same contrasting colour. */
private const val ARTIST_ALPHA = 0.74f
