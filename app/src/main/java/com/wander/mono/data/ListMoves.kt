package com.wander.mono.data

/** [item] moved [by] places (negative is up), clamped to the list. Unchanged when [item] is absent. */
internal fun <T> List<T>.moved(item: T, by: Int): List<T> {
    val from = indexOf(item)
    if (from < 0) return this
    val to = (from + by).coerceIn(0, lastIndex)
    return toMutableList().apply { add(to, removeAt(from)) }
}
