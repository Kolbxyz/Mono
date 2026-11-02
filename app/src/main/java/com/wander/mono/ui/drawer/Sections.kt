package com.wander.mono.ui.drawer

import java.text.Collator
import java.util.Locale

/**
 * The letter [label] is filed under: "#" for names that do not start with a letter. A letter the
 * collator sorts as a variant of a base letter goes under that letter ("É" and "Ø" under "E" and
 * "O" in English), so sections follow the same collator order the catalog sorts with and each
 * letter's apps stay in one run. A letter of its own in the locale (Danish "Ø") keeps its own.
 */
internal fun sectionLetter(label: String, collator: Collator = primaryCollator()): String {
    val name = label.trim()
    val first = if (name.isEmpty()) "" else name.substring(0, name.offsetByCodePoints(0, 1))
    return when {
        first.isEmpty() || !Character.isLetter(first.codePointAt(0)) -> OTHER
        else -> BASE_LETTERS.firstOrNull { collator.compare(it, first) == 0 }
            ?: AppSearch.normalize(first).uppercase(Locale.ROOT)
    }
}

/** One section per letter, at the first app using it, in list order. */
internal fun sectionsOf(labels: List<String>, locale: Locale = Locale.getDefault()): List<Section> {
    val collator = primaryCollator(locale)
    val seen = mutableSetOf<String>()
    return labels.mapIndexedNotNull { index, label ->
        val letter = sectionLetter(label, collator)
        if (seen.add(letter)) Section(letter, index) else null
    }
}

/** Ignores accents and case, as the section headers do; a new one per call, since it is not thread safe. */
internal fun primaryCollator(locale: Locale = Locale.getDefault()): Collator =
    Collator.getInstance(locale).apply { strength = Collator.PRIMARY }

private val BASE_LETTERS = ('A'..'Z').map(Char::toString)

private const val OTHER = "#"
