package com.wander.mono.ui.home

import android.icu.text.DateFormat
import java.text.CharacterIterator
import java.util.Date
import java.util.Locale

/**
 * One character of the clock; an AM/PM [marker] is set small beside the digits. [slot] is its
 * place in the clock (see [glyphSlots]), what its roll animation is keyed by.
 */
internal data class ClockGlyph(val char: Char, val marker: Boolean, val slot: String)

/**
 * The time as glyphs, the AM/PM marker flagged wherever the locale puts it: ICU reports the
 * fields of what it formatted.
 */
internal fun clockGlyphs(millis: Long, skeleton: String, locale: Locale): List<ClockGlyph> {
    val iterator = DateFormat.getInstanceForSkeleton(skeleton, locale).formatToCharacterIterator(Date(millis))
    val chars = ArrayList<Char>()
    val markers = ArrayList<Boolean>()
    val fields = ArrayList<String?>()
    var c = iterator.first()
    while (c != CharacterIterator.DONE) {
        val attributes = iterator.attributes
        chars += c
        markers += attributes.containsKey(DateFormat.Field.AM_PM)
        fields += attributes.keys.firstOrNull { it is DateFormat.Field }?.toString()
        c = iterator.next()
    }
    val slots = glyphSlots(fields)
    return chars.indices.map { ClockGlyph(chars[it], markers[it], slots[it]) }
}

/** Glyphs for one field (the stacked clock's hours or minutes), slotted from the end like [glyphSlots]. */
internal fun fieldGlyphs(text: String, field: String): List<ClockGlyph> {
    val slots = glyphSlots(List(text.length) { field })
    return text.mapIndexed { index, char -> ClockGlyph(char, marker = false, slot = slots[index]) }
}

/**
 * Each glyph's place in the clock, stable while the time changes: the field it belongs to ([fields],
 * null for literal text, numbered in order) and how far it sits from that run's end. "9:59" to
 * "10:00" then rolls the hour's last digit and adds its first, instead of shifting every glyph.
 */
internal fun glyphSlots(fields: List<String?>): List<String> {
    val slots = ArrayList<String>(fields.size)
    var start = 0
    var literals = 0
    while (start < fields.size) {
        var end = start
        while (end < fields.size && fields[end] == fields[start]) end++
        val run = fields[start] ?: "literal${literals++}"
        for (index in start until end) slots += "$run:${end - 1 - index}"
        start = end
    }
    return slots
}
