package com.wander.mono.ui.drawer

import kotlin.math.abs

/**
 * Whether [a] and [b] are at most one edit apart, an edit being one character added, removed,
 * replaced, or two neighbours swapped (a Damerau-Levenshtein distance of at most 1). Bounded to one,
 * it only walks the strings once instead of filling a distance table.
 */
internal fun withinOneEdit(a: String, b: String): Boolean {
    if (abs(a.length - b.length) > 1) return false
    var same = 0
    while (same < a.length && same < b.length && a[same] == b[same]) same++
    return when {
        same == a.length && same == b.length -> true
        a.length == b.length -> a.endsMatch(same + 1, b, same + 1) || a.swappedAt(same, b)
        a.length > b.length -> a.endsMatch(same + 1, b, same)
        else -> a.endsMatch(same, b, same + 1)
    }
}

/** Whether what follows [from] in this string equals what follows [otherFrom] in [other]. */
private fun String.endsMatch(from: Int, other: String, otherFrom: Int): Boolean =
    length - from == other.length - otherFrom && regionMatches(from, other, otherFrom, length - from)

/** Whether the two characters at [at] are swapped in [other] and everything after them is equal. */
private fun String.swappedAt(at: Int, other: String): Boolean =
    at + 1 < length && this[at] == other[at + 1] && this[at + 1] == other[at] && endsMatch(at + 2, other, at + 2)
