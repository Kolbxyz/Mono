package com.wander.mono.data.settings

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * One user setting: its DataStore key, its value when never set, and how it is read and written.
 * Features declare theirs as constants in their own `*Settings.kt` file next to this one.
 */
sealed interface Setting<T> {
    val default: T

    fun read(prefs: Preferences): T

    fun write(prefs: MutablePreferences, value: T)
}

/** An on/off setting. */
class BooleanSetting(name: String, override val default: Boolean) : Setting<Boolean> {
    private val key = booleanPreferencesKey(name)

    override fun read(prefs: Preferences): Boolean = prefs[key] ?: default

    override fun write(prefs: MutablePreferences, value: Boolean) {
        prefs[key] = value
    }
}

/** An enum whose stored form is fixed by [stored], so constants can be renamed without losing choices. */
interface StoredEnum {
    val stored: String
}

/**
 * A choice among [values]. A stored value no constant claims (written by a newer build, then
 * downgraded) reads as [default], the same as never having chosen.
 */
class EnumSetting<E>(
    name: String,
    override val default: E,
    val values: List<E>,
) : Setting<E> where E : Enum<E>, E : StoredEnum {
    private val key = stringPreferencesKey(name)

    init {
        require(values.map { it.stored }.toSet().size == values.size) { "Duplicate stored value in $name" }
    }

    fun decode(stored: String?): E = values.firstOrNull { it.stored == stored } ?: default

    override fun read(prefs: Preferences): E = decode(prefs[key])

    override fun write(prefs: MutablePreferences, value: E) {
        prefs[key] = value.stored
    }
}
