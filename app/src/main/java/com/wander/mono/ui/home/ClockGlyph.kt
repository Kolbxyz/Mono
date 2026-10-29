package com.wander.mono.ui.home

import android.icu.text.DateFormat
import java.text.CharacterIterator
import java.util.Date
import java.util.Locale

/** One character of the clock; an AM/PM [marker] is set small beside the digits. */
internal data class ClockGlyph(val char: Char, val marker: Boolean)

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
