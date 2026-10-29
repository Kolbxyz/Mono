package com.wander.mono.data

/** [item] moved [by] places (negative is up), clamped to the list. Unchanged when [item] is absent. */
internal fun <T> List<T>.moved(item: T, by: Int): List<T> {
    val from = indexOf(item)
    if (from < 0) return this
    val to = (from + by).coerceIn(0, lastIndex)
    return toMutableList().apply { add(to, removeAt(from)) }
}

/**
 * The items of [order] put, in that order, into the places they occupy in this list; items not in
 * [order] (an uninstalled favourite) keep their place. Items of [order] not in this list are ignored.
 */
internal fun <T> List<T>.reordered(order: List<T>): List<T> {
    val moving = order.filter { it in this }.distinct()
    val queue = ArrayDeque(moving)
    return map { if (it in moving) queue.removeFirst() else it }
}
