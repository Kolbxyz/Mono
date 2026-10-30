package com.wander.mono.ui.drawer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.components.rememberMonoHaptics
import com.wander.mono.ui.home.onWallpaper
import com.wander.mono.ui.theme.LocalReducedMotion

/**
 * The fast-scroll rail, on the end edge or, [atStart], the start edge: ★ for the top of the list,
 * then [sections]' letters. Sliding a finger along it jumps the list to each entry ([onSelect] takes
 * the rail index; [current] is one too), shows the entry in a bubble and ticks once per entry.
 * Sliding the finger off the rail toward the list bows the letters around it out in a wave that
 * follows the finger, so it can scrub from wherever the thumb rests; wave and bubble face the list
 * whichever edge the rail is on. While scrubbing, the letter nearest the finger grows and its
 * neighbours less so. The letters sit straight on the drawer's background, shadowed like other text
 * on the wallpaper. With reduced motion the wave stays flat, nothing grows and only the letter under
 * the finger lights up. With a private space ([lock]) a lock ends the rail: scrubbing onto it
 * jumps to the space like a letter, tapping it alone locks or unlocks it. Letters tick lightly, ★
 * and the lock with a long buzz.
 * TalkBack sees one adjustable control: swiping up or down moves to the next or previous entry,
 * with the same ticks, and the lock is a custom action. A [scrub] begun on home's edge drives the
 * same finger ([RailFinger]) as the rail's own touch: bubble, wave, ticks and jumps alike.
 */
@Composable
fun LetterRail(
    sections: List<Section>,
    current: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    atStart: Boolean = false,
    lock: RailLock? = null,
    scrub: RailScrub? = null,
) {
    val view = LocalView.current
    val haptics = rememberMonoHaptics()
    val motion = MaterialTheme.motionScheme
    val reducedMotion = LocalReducedMotion.current
    val mirror = RailMath.mirror(rtl = LocalLayoutDirection.current == LayoutDirection.Rtl, atStart = atStart)
    val top = stringResource(R.string.drawer_rail_top)
    val topSpoken = stringResource(R.string.drawer_rail_top_spoken)
    // The lock's slot holds no letter: it draws a glyph instead, and so does the bubble over it.
    val letters = remember(sections, top, lock != null) {
        listOf(top) + sections.map { it.letter } + listOfNotNull("".takeIf { lock != null })
    }
    val lockIndex = if (lock != null) letters.lastIndex else -1
    val density = LocalDensity.current
    val pressBow = with(density) { PRESS_BOW.toPx() }
    // A drawn letter needs one labelMedium line, at the user's font scale.
    val minSlot = with(density) { MaterialTheme.typography.labelMedium.lineHeight.toPx() }
    val lettersTop = with(density) { TRACK_PADDING.toPx() }
    val finger = rememberRailFinger(
        RailFingerInputs(
            letters = letters,
            lettersTop = lettersTop,
            hasLock = lock != null,
            mirror = mirror,
            reducedMotion = reducedMotion,
            bowLimit = with(density) { WAVE_AMPLITUDE.toPx() },
            onSelect = onSelect,
            onTick = { haptics.railTick(it) },
        ),
    )
    val currentLock by rememberUpdatedState(lock)
    var height by remember { mutableIntStateOf(0) }
    val placed = remember { RailPlacement() }
    if (scrub != null) FollowRailScrub(scrub, finger) { placed.coordinates }
    // A small bow on touch, eased in, so the press shows before the finger moves.
    val pressed by animateFloatAsState(if (finger.scrubbing && !reducedMotion) 1f else 0f, motion.fastSpatialSpec())

    val railLabel = stringResource(R.string.drawer_rail)
    val privateSpoken = stringResource(R.string.drawer_rail_private)
    val spoken = remember(letters, topSpoken, privateSpoken, lockIndex) {
        railSpoken(letters, topSpoken, privateSpoken, hasLock = lockIndex >= 0)
    }
    val lockAction = rememberLockActions(lock)
    // Only a screen reader moves the rail through its semantics; it feels the same ticks.
    val spokenSelect: (Int) -> Unit = { index ->
        if (view.screenReaderOn()) haptics.railTick(RailEntries.tickFor(index, letters.size, hasLock = lock != null))
        onSelect(index)
    }
    Box(
        modifier
            .fillMaxHeight()
            .width(RAIL_WIDTH)
            .onSizeChanged { height = it.height }
            .onGloballyPositioned { placed.coordinates = it }
            .railSemantics(railLabel, spoken, current, spokenSelect, lockAction)
            .pointerInput(finger) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    finger.press(down.position.x, down.position.y)
                    do {
                        val event = awaitPointerEvent()
                        event.changes.filter { it.pressed }.forEach { change ->
                            change.consume()
                            finger.move(change.position.x, change.position.y)
                        }
                    } while (event.changes.any { it.pressed })
                    // A tap on the lock, not a scrub through it, locks or unlocks the space.
                    if (finger.lift()) currentLock?.onToggle?.invoke()
                }
            },
    ) {
        // A short rail (landscape) draws every other or third letter; all of them still scrub.
        val stride = RailMath.labelStride(finger.lettersHeight.toFloat(), letters.size, minSlot)
        // No clip: the wave bows letters out past the track, toward the list.
        Column(
            Modifier
                .fillMaxHeight()
                .padding(horizontal = TRACK_INSET, vertical = TRACK_PADDING)
                .onSizeChanged { finger.lettersHeight = it.height },
        ) {
            val letterStyle = MaterialTheme.typography.labelMedium.onWallpaper()
            letters.forEachIndexed { index, letter ->
                val lit = finger.scrubbing && index == finger.active
                val wave = Modifier.graphicsLayer {
                    val column = finger.lettersHeight.toFloat()
                    val slot = RailMath.fingerSlot(finger.fingerY, lettersTop, column, letters.size)
                    val bow = pressBow * pressed + finger.dragBow
                    translationX = -mirror * RailMath.letterOffset(index, slot, bow)
                    // Grows with the press, so it eases back to size on release.
                    scaleX = RailMath.letterScale(index, slot, pressed)
                    scaleY = scaleX
                }
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (index == lockIndex && lock != null) {
                        RailLockGlyph(lock, lit, wave)
                    } else if (lit || index % stride == 0) {
                        Text(
                            letter,
                            style = letterStyle,
                            fontWeight = if (lit) FontWeight.Bold else null,
                            color = if (lit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = wave,
                        )
                    }
                }
            }
        }
        RailBubble(
            letter = finger.bubbleLetter,
            symbol = lock?.symbol?.takeIf { finger.bubbleLetter.isEmpty() },
            visible = finger.scrubbing,
            fingerY = { finger.fingerY },
            railHeight = { height.toFloat() },
            bow = { pressBow * pressed + finger.dragBow },
            mirror = mirror,
            modifier = Modifier.align(if (mirror > 0f) AbsoluteAlignment.TopLeft else AbsoluteAlignment.TopRight),
        )
    }
}

/** Where the rail sits in the window, for mapping a scrub from home onto it; not state, read on each move. */
private class RailPlacement {
    var coordinates: LayoutCoordinates? = null
}

/** Width of the rail's touch area, which the list leaves free on the rail's side. */
internal val RAIL_WIDTH = 48.dp

/** Space on each side of the letters' track, inside the rail's touch area. */
private val TRACK_INSET = 8.dp

/** Space at each end of the track, above the first letter and below the last. */
private val TRACK_PADDING = 8.dp

/**
 * The furthest the letter under the finger moves out, however far the finger slides toward the list;
 * its neighbours move less, over [RailMath.WAVE_REACH] letters.
 */
internal val WAVE_AMPLITUDE = 72.dp

/** How far the letter under the finger bows out on touch alone, before the finger slides. */
private val PRESS_BOW = 8.dp
