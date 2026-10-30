package com.wander.mono.ui.drawer

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

/**
 * [ranges] within a text of [length], in order, overlapping or touching ones joined, the parts
 * outside the text cut off. What the query matched can come as one range per letter (initials).
 */
internal fun mergedRanges(ranges: List<IntRange>, length: Int): List<IntRange> {
    val merged = ArrayList<IntRange>(ranges.size)
    ranges.map { maxOf(it.first, 0)..minOf(it.last, length - 1) }
        .filterNot { it.isEmpty() }
        .sortedBy { it.first }
        .forEach { range ->
            val last = merged.lastOrNull()
            if (last != null && range.first <= last.last + 1) {
                merged[merged.lastIndex] = last.first..maxOf(last.last, range.last)
            } else {
                merged.add(range)
            }
        }
    return merged
}

/** [text] with the characters in [ranges] drawn in [emphasis]. */
internal fun highlighted(text: String, ranges: List<IntRange>, emphasis: SpanStyle): AnnotatedString =
    buildAnnotatedString {
        append(text)
        mergedRanges(ranges, text.length).forEach { addStyle(emphasis, it.first, it.last + 1) }
    }

/** [text] with what the query matched in the theme's primary colour and bold, the rest as it was. */
@Composable
internal fun rememberHighlighted(text: String, ranges: List<IntRange>): AnnotatedString {
    val color = MaterialTheme.colorScheme.primary
    return remember(text, ranges, color) {
        if (ranges.isEmpty()) {
            AnnotatedString(text)
        } else {
            highlighted(text, ranges, SpanStyle(color = color, fontWeight = FontWeight.Bold))
        }
    }
}
