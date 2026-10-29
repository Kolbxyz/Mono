package com.wander.mono.ui.drawer

import android.os.Build
import android.view.HapticFeedbackConstants
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.theme.LocalReducedMotion
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The fast-scroll rail on the end edge. Sliding a finger along it jumps the list to each letter,
 * shows the letter in a bubble and ticks once per letter. Sliding the finger off the rail toward the
 * list bows the letters around it out in a wave that follows the finger, so it can scrub from
 * wherever the thumb rests. With reduced motion the wave stays flat and only the letter under the
 * finger lights up.
 * TalkBack sees one adjustable control: swiping up or down moves to the next or previous letter.
 */
@Composable
fun LetterRail(
    sections: List<Section>,
    current: Int,
    onSelect: (Section) -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val motion = MaterialTheme.motionScheme
    val reducedMotion = LocalReducedMotion.current
    val mirror = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    val bowLimit = with(LocalDensity.current) { WAVE_AMPLITUDE.toPx() }
    val pressBow = with(LocalDensity.current) { PRESS_BOW.toPx() }
    val scope = rememberCoroutineScope()
    // A drawn letter needs one labelMedium line, at the user's font scale.
    val minSlot = with(LocalDensity.current) { MaterialTheme.typography.labelMedium.lineHeight.toPx() }
    val currentSections by rememberUpdatedState(sections)
    val currentOnSelect by rememberUpdatedState(onSelect)
    // Read by the long-lived pointer handler, so it sees a changed setting or layout direction.
    val currentMirror by rememberUpdatedState(mirror)
    val currentReducedMotion by rememberUpdatedState(reducedMotion)

    var height by remember { mutableIntStateOf(0) }
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

    fun follow(y: Float) {
        fingerY = y
        val index = RailMath.letterAt(y, height.toFloat(), currentSections.size)
        if (index < 0 || index == active) return
        active = index
        bubbleLetter = currentSections[index].letter
        view.performHapticFeedback(LETTER_TICK)
        currentOnSelect(currentSections[index])
    }

    val railLabel = stringResource(R.string.drawer_rail)
    val currentLetter = sections.getOrNull(current)?.letter.orEmpty()
    Box(
        modifier
            .fillMaxHeight()
            .width(RAIL_WIDTH)
            .onSizeChanged { height = it.height }
            .clearAndSetSemantics {
                contentDescription = railLabel
                stateDescription = currentLetter
                if (sections.size > 1) {
                    progressBarRangeInfo = ProgressBarRangeInfo(
                        current = current.toFloat(),
                        range = 0f..sections.lastIndex.toFloat(),
                        steps = sections.size - 2,
                    )
                    setProgress { target ->
                        val index = target.roundToInt().coerceIn(0, sections.lastIndex)
                        if (index != current) onSelect(sections[index])
                        true
                    }
                }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    settle?.cancel()
                    dragBow = 0f
                    scrubbing = true
                    follow(down.position.y)
                    do {
                        val event = awaitPointerEvent()
                        event.changes.filter { it.pressed }.forEach { change ->
                            change.consume()
                            follow(change.position.y)
                            if (!currentReducedMotion) {
                                val travel = RailMath.travel(down.position.x, change.position.x, currentMirror)
                                dragBow = RailMath.bow(travel, bowLimit)
                            }
                        }
                    } while (event.changes.any { it.pressed })
                    scrubbing = false
                    active = -1
                    val from = dragBow
                    settle = scope.launch {
                        animate(from, 0f, animationSpec = springBack) { value, _ -> dragBow = value }
                    }
                }
            },
    ) {
        // A short rail (landscape) draws every other or third letter; all of them still scrub.
        val stride = RailMath.labelStride(height.toFloat(), sections.size, minSlot)
        // The letters sit on a slim tonal pill, so the rail reads as one control.
        Column(
            Modifier
                .fillMaxHeight()
                .padding(horizontal = TRACK_INSET)
                // A shaped background, not a clip: the wave bows letters out past the pill.
                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                .padding(vertical = 8.dp),
        ) {
            sections.forEachIndexed { index, section ->
                val lit = scrubbing && index == active
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (lit || index % stride == 0) {
                        Text(
                            section.letter,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (lit) FontWeight.Bold else null,
                            color = if (lit) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.graphicsLayer {
                                val slot = RailMath.fingerSlot(fingerY, height.toFloat(), sections.size)
                                val bow = pressBow * pressed + dragBow
                                translationX = -mirror * RailMath.letterOffset(index, slot, bow)
                            },
                        )
                    }
                }
            }
        }
        RailBubble(
            letter = bubbleLetter,
            visible = scrubbing,
            fingerY = { fingerY },
            bow = { pressBow * pressed + dragBow },
            modifier = Modifier.align(Alignment.TopStart),
        )
    }
}

/** Width of the rail's touch area, which the list leaves free at its end. */
internal val RAIL_WIDTH = 48.dp

/** Space on each side of the rail's pill, inside its touch area. */
private val TRACK_INSET = 8.dp

/**
 * The furthest the letter under the finger moves out, however far the finger slides toward the list;
 * its neighbours move less, over [RailMath.WAVE_REACH] letters.
 */
internal val WAVE_AMPLITUDE = 72.dp

/** How far the letter under the finger bows out on touch alone, before the finger slides. */
private val PRESS_BOW = 8.dp

/** The light "segment" tick Android 14 added for scrubbing through values; a clock tick before it. */
private val LETTER_TICK = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
} else {
    HapticFeedbackConstants.CLOCK_TICK
}
