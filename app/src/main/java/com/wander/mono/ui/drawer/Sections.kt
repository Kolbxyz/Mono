package com.wander.mono.ui.drawer

/** "#" for names that do not start with a letter; accents fold onto their base letter ("É" is "E"). */
internal fun sectionLetter(label: String): String {
    val first = AppSearch.normalize(label).firstOrNull() ?: return OTHER
    return if (first.isLetter()) first.uppercase() else OTHER
}

/** One section per letter, at the first app using it, in list order. */
internal fun sectionsOf(labels: List<String>): List<Section> {
    val seen = mutableSetOf<String>()
    return labels.mapIndexedNotNull { index, label ->
        val letter = sectionLetter(label)
        if (seen.add(letter)) Section(letter, index) else null
    }
}

private const val OTHER = "#"
