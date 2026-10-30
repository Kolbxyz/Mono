package com.wander.mono.data.backup

import com.wander.mono.core.apps.AppKey

internal fun appKey(encoded: Any?): AppKey = AppKey.decode(string(encoded)) ?: throw MalformedBackup()

internal fun string(value: Any?): String = value as? String ?: throw MalformedBackup()

internal inline fun <reified T> Map<*, *>.field(name: String): T = this[name] as? T ?: throw MalformedBackup()

/** A field added after the first version: absent in older files, so absent reads as [absent]. */
internal inline fun <reified T> Map<*, *>.optionalField(name: String, absent: T): T =
    if (this[name] == null) absent else field(name)

internal fun Map<*, *>.strings(name: String): List<String> = field<List<*>>(name).map(::string)

internal fun Number.whole(): Long = integer(this) ?: throw MalformedBackup()

/** Whole numbers only: JSON parsers hand them over as Int or Long depending on size. */
internal fun integer(value: Any?): Long? = when (value) {
    is Int -> value.toLong()
    is Long -> value
    else -> null
}
