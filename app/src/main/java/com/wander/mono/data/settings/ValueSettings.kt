package com.wander.mono.data.settings

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * A whole number within [range], such as a size or a weight. A stored value outside it (the range
 * narrowed in a later build, or a hand-edited backup) reads clamped, so a slider never starts off
 * its track.
 */
class IntSetting(override val name: String, override val default: Int, val range: IntRange) : Setting<Int> {
    private val key = intPreferencesKey(name)

    init {
        require(default in range) { "Default of $name outside its range" }
    }

    override fun read(prefs: Preferences): Int = (prefs[key] ?: default).coerceIn(range)

    override fun write(prefs: MutablePreferences, value: Int) {
        prefs[key] = value.coerceIn(range)
    }

    override fun toStored(value: Int): Any = value

    /** JSON hands numbers back as whatever width fits; only whole numbers are taken. */
    override fun fromStored(value: Any): Int? = when (value) {
        is Int -> value
        is Long -> value.takeIf { it in Int.MIN_VALUE..Int.MAX_VALUE }?.toInt()
        else -> null
    }?.coerceIn(range)
}

/** A point in time, such as an epoch-millis deadline. */
class LongSetting(override val name: String, override val default: Long) : Setting<Long> {
    private val key = longPreferencesKey(name)

    override fun read(prefs: Preferences): Long = prefs[key] ?: default

    override fun write(prefs: MutablePreferences, value: Long) {
        prefs[key] = value
    }

    override fun toStored(value: Long): Any = value

    override fun fromStored(value: Any): Long? = when (value) {
        is Long -> value
        is Int -> value.toLong()
        else -> null
    }
}

/** Text chosen from a list the app builds at run time, such as a package name. */
class StringSetting(override val name: String, override val default: String) : Setting<String> {
    private val key = stringPreferencesKey(name)

    override fun read(prefs: Preferences): String = prefs[key] ?: default

    override fun write(prefs: MutablePreferences, value: String) {
        prefs[key] = value
    }

    override fun toStored(value: String): Any = value

    override fun fromStored(value: Any): String? = value as? String
}
