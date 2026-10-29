package com.wander.mono.ui.home

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.wander.mono.data.settings.ClockAlignment
import com.wander.mono.data.settings.ClockLook
import com.wander.mono.data.settings.ClockStyle
import com.wander.mono.ui.theme.CLOCK_MAX_FONT_SCALE
import com.wander.mono.ui.theme.capFontScale
import com.wander.mono.ui.theme.clockStyle
import java.text.CharacterIterator
import java.util.Date
import java.util.Locale

/** One character of the clock; an AM/PM [marker] is set small beside the digits. */
internal data class ClockGlyph(val char: Char, val marker: Boolean)

/**
 * The time in the user's [look]: one line, hours over minutes, or one small line. Each character
 * rolls up on its own when it changes, on the theme's motion (instant with reduced motion).
 * [compact] (a phone in landscape) shrinks it so the list keeps room.
 */
@Composable
fun ClockText(
    now: Long,
    is24Hour: Boolean,
    look: ClockLook,
    locale: Locale,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val size = clockSize(look, compact)
    val style = clockStyle(look.font, look.weight, size.sp).capFontScale(CLOCK_MAX_FONT_SCALE).onWallpaper()
    val horizontal = if (look.alignment == ClockAlignment.Center) Alignment.CenterHorizontally else Alignment.Start
    if (look.style == ClockStyle.Stacked) {
        val hours = remember(now, is24Hour, locale) { pattern(now, if (is24Hour) "HH" else "hh", locale) }
        val minutes = remember(now, locale) { pattern(now, "mm", locale) }
        Column(modifier, horizontalAlignment = horizontal) {
            RollingGlyphs(hours.map { ClockGlyph(it, marker = false) }, style)
            RollingGlyphs(minutes.map { ClockGlyph(it, marker = false) }, style)
        }
    } else {
        val skeleton = if (is24Hour) "Hm" else "hm"
        val glyphs = remember(now, skeleton, locale) { clockGlyphs(now, skeleton, locale) }
        Column(modifier, horizontalAlignment = horizontal) { RollingGlyphs(glyphs, style) }
    }
}

/** The clock's size in sp for its style; landscape caps it so the list keeps room. */
internal fun clockSize(look: ClockLook, compact: Boolean): Int {
    val size = if (look.style == ClockStyle.Small) look.size * SMALL_FRACTION else look.size.toFloat()
    return (if (compact) size * COMPACT_FRACTION else size).toInt()
}

@Composable
private fun RollingGlyphs(glyphs: List<ClockGlyph>, style: TextStyle) {
    val motion = MaterialTheme.motionScheme
    Row {
        glyphs.forEachIndexed { index, glyph ->
            AnimatedContent(
                targetState = glyph,
                modifier = Modifier.alignByBaseline(),
                transitionSpec = {
                    (slideInVertically(motion.defaultSpatialSpec()) { it } + fadeIn(motion.defaultEffectsSpec()))
                        .togetherWith(
                            slideOutVertically(motion.defaultSpatialSpec()) { -it } + fadeOut(motion.fastEffectsSpec()),
                        )
                },
                label = "clockGlyph$index",
            ) { shown ->
                val glyphStyle = if (shown.marker) style.copy(fontSize = style.fontSize * MARKER_FRACTION) else style
                Text(shown.char.toString(), style = glyphStyle, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/**
 * The time as glyphs, the AM/PM marker flagged wherever the locale puts it: ICU reports the
 * fields of what it formatted.
 */
internal fun clockGlyphs(millis: Long, skeleton: String, locale: Locale): List<ClockGlyph> {
    val iterator = DateFormat.getInstanceForSkeleton(skeleton, locale).formatToCharacterIterator(Date(millis))
    val glyphs = ArrayList<ClockGlyph>()
    var c = iterator.first()
    while (c != CharacterIterator.DONE) {
        val marker = iterator.attributes.containsKey(DateFormat.Field.AM_PM)
        glyphs += ClockGlyph(c, marker)
        c = iterator.next()
    }
    return glyphs
}

private fun pattern(millis: Long, pattern: String, locale: Locale): String =
    SimpleDateFormat(pattern, locale).format(Date(millis))

/** ICU picks the right order and separators for the locale from a skeleton. */
internal fun format(millis: Long, skeleton: String, locale: Locale): String =
    DateFormat.getInstanceForSkeleton(skeleton, locale).format(Date(millis))

/** The AM/PM marker, relative to the clock digits. */
private const val MARKER_FRACTION = 0.4f

private const val SMALL_FRACTION = 0.5f
private const val COMPACT_FRACTION = 0.64f
