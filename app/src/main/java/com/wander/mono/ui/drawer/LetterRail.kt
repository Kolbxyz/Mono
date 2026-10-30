package com.wander.mono.ui.drawer

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.wander.mono.ui.theme.LocalReducedMotion
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The fast-scroll rail, on the end edge or, [atStart], the start edge: ★ for the top of the list,
 * then [sections]' letters. Sliding a finger along it jumps the list to each entry ([onSelect] takes
 * the rail index; [current] is one too), shows the entry in a bubble and ticks once per entry.
 * Sliding the finger off the rail toward the list bows the letters around it out in a wave that
 * follows the finger, so it can scrub from wherever the thumb rests; wave and bubble face the list
 * whichever edge the rail is on. With reduced motion the wave stays flat and only the letter under
 * the finger lights up. With a private space ([lock]) a lock ends the rail: scrubbing onto it
 * jumps to the space like a letter, tapping it alone locks or unlocks it. Letters tick lightly, ★
 * and the lock with a long buzz.
 * TalkBack sees one adjustable control: swiping up or down moves to the next or previous entry,
 * with the same ticks, and the lock is a custom action.
 */
@Composable
fun LetterRail(
    sections: List<Section>,
    current: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    atStart: Boolean = false,
    lock: RailLock? = null,
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
    val bowLimit = with(LocalDensity.current) { WAVE_AMPLITUDE.toPx() }
    val pressBow = with(LocalDensity.current) { PRESS_BOW.toPx() }
    val scope = rememberCoroutineScope()
    // A drawn letter needs one labelMedium line, at the user's font scale.
    val minSlot = with(LocalDensity.current) { MaterialTheme.typography.labelMedium.lineHeight.toPx() }
    val currentLetters by rememberUpdatedState(letters)
    val currentOnSelect by rememberUpdatedState(onSelect)
    // Read by the long-lived pointer handler, so it sees a changed setting or layout direction.
    val currentMirror by rememberUpdatedState(mirror)
    val currentReducedMotion by rememberUpdatedState(reducedMotion)
    val currentLock by rememberUpdatedState(lock)

    var height by remember { mutableIntStateOf(0) }
    // The letters' own column, inside the pill's end padding: what a finger's y maps onto.
    var lettersHeight by remember { mutableIntStateOf(0) }
    val lettersTop = with(LocalDensity.current) { TRACK_PADDING.toPx() }
    val currentLettersTop by rememberUpdatedState(lettersTop)
    var fingerY by remember { mutableFloatStateOf(0f) }
    var active by remember { mutableIntStateOf(-1) }
    // Unlike [active], kept after release, so the bubble fades out still showing the last letter.
    var bubbleLetter by remember { mutableStateOf("") }
    var scrubbing by remember { mutableStateOf(false) }
    // A small bow on touch, eased in, so the press shows before the finger moves.
    val pressed by animateFloatAsState(if (scrubbing && !reducedMotion) 1f else 0f, motion.fastSpatialSpec())
    // How far the letter under the finger bows out from the finger's slide toward the list: set
    // straight from each move so it tracks without lag, eased back to the rail on release.
    var dragBow by remember { mutableFloatStateOf(0f) }
    var settle by remember { mutableStateOf<Job?>(null) }
    val springBack = motion.fastSpatialSpec<Float>()

    /** Moves to the entry under [y]; true when that is another entry than before. */
    fun follow(y: Float): Boolean {
        fingerY = y
        val count = currentLetters.size
        val index = RailMath.letterAt(y, currentLettersTop, lettersHeight.toFloat(), count)
        if (index < 0 || index == active) return false
        active = index
        bubbleLetter = currentLetters[index]
        haptics.railTick(RailMath.tickFor(index, count, hasLock = currentLock != null))
        currentOnSelect(index)
        return true
    }

    val railLabel = stringResource(R.string.drawer_rail)
    val privateSpoken = stringResource(R.string.drawer_rail_private)
    val spoken = remember(letters, topSpoken, privateSpoken, lockIndex) {
        railSpoken(letters, topSpoken, privateSpoken, hasLock = lockIndex >= 0)
    }
    val lockAction = rememberLockActions(lock)
    // Only a screen reader moves the rail through its semantics; it feels the same ticks.
    val spokenSelect: (Int) -> Unit = { index ->
        if (view.screenReaderOn()) haptics.railTick(RailMath.tickFor(index, letters.size, hasLock = lock != null))
        onSelect(index)
    }
    Box(
        modifier
            .fillMaxHeight()
            .width(RAIL_WIDTH)
            .onSizeChanged { height = it.height }
            .railSemantics(railLabel, spoken, current, spokenSelect, lockAction)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    settle?.cancel()
                    dragBow = 0f
                    scrubbing = true
                    follow(down.position.y)
                    // A tap on the lock, not a scrub through it, locks or unlocks the space.
                    val downEntry = active
                    var stayed = true
                    do {
                        val event = awaitPointerEvent()
                        event.changes.filter { it.pressed }.forEach { change ->
                            change.consume()
                            if (follow(change.position.y)) stayed = false
                            if (!currentReducedMotion) {
                                val travel = RailMath.travel(down.position.x, change.position.x, currentMirror)
                                dragBow = RailMath.bow(travel, bowLimit)
                            }
                        }
                    } while (event.changes.any { it.pressed })
                    scrubbing = false
                    val tappedLock = stayed && downEntry == currentLetters.lastIndex
                    currentLock?.takeIf { tappedLock }?.onToggle?.invoke()
                    active = -1
                    val from = dragBow
                    settle = scope.launch {
                        animate(from, 0f, animationSpec = springBack) { value, _ -> dragBow = value }
                    }
                }
            },
    ) {
        // A short rail (landscape) draws every other or third letter; all of them still scrub.
        val stride = RailMath.labelStride(lettersHeight.toFloat(), letters.size, minSlot)
        // The letters sit on a slim tonal pill, so the rail reads as one control.
        Column(
            Modifier
                .fillMaxHeight()
                .padding(horizontal = TRACK_INSET)
                // A shaped background, not a clip: the wave bows letters out past the pill.
                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                .padding(vertical = TRACK_PADDING)
                .onSizeChanged { lettersHeight = it.height },
        ) {
            letters.forEachIndexed { index, letter ->
                val lit = scrubbing && index == active
                val wave = Modifier.graphicsLayer {
                    val slot = RailMath.fingerSlot(fingerY, lettersTop, lettersHeight.toFloat(), letters.size)
                    val bow = pressBow * pressed + dragBow
                    translationX = -mirror * RailMath.letterOffset(index, slot, bow)
                }
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (index == lockIndex && lock != null) {
                        RailLockGlyph(lock, lit, wave)
                    } else if (lit || index % stride == 0) {
                        Text(
                            letter,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (lit) FontWeight.Bold else null,
                            color = if (lit) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = wave,
                        )
                    }
                }
            }
        }
        RailBubble(
            letter = bubbleLetter,
            symbol = lock?.symbol?.takeIf { bubbleLetter.isEmpty() },
            visible = scrubbing,
            fingerY = { fingerY },
            railHeight = { height.toFloat() },
            bow = { pressBow * pressed + dragBow },
            mirror = mirror,
            modifier = Modifier.align(if (mirror > 0f) AbsoluteAlignment.TopLeft else AbsoluteAlignment.TopRight),
        )
    }
}

/** Width of the rail's touch area, which the list leaves free on the rail's side. */
internal val RAIL_WIDTH = 48.dp

/** Space on each side of the rail's pill, inside its touch area. */
private val TRACK_INSET = 8.dp

/** Space inside each end of the pill, above the first letter and below the last. */
private val TRACK_PADDING = 8.dp

/**
 * The furthest the letter under the finger moves out, however far the finger slides toward the list;
 * its neighbours move less, over [RailMath.WAVE_REACH] letters.
 */
internal val WAVE_AMPLITUDE = 72.dp

/** How far the letter under the finger bows out on touch alone, before the finger slides. */
private val PRESS_BOW = 8.dp
