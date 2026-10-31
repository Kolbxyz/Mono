package com.wander.mono.ui.drawer

import kotlin.math.roundToInt

/** An alarm's time of day, on the 24-hour clock. */
data class AlarmTime(val hour: Int, val minute: Int)

/**
 * Reads a search query as a timer ("timer 10m", "10 min timer", "timer 1h30", "5 minutes") or an
 * alarm ("alarm 7:30", "alarm 7am", "wake 6:45"), in English or French ("minuteur 10 min",
 * "réveil 7h30", "alarme à 7h"), whatever the case or accents. Times read on the 24-hour clock, or
 * the 12-hour one with am/pm.
 *
 * A duration needs the word "timer" unless one of its units is spelled out ("5 min", not "5m"),
 * so a short code like "5s" stays a search. A time always needs "alarm", "wake" or their French.
 */
internal object ClockQuery {
    /** The longest timer `AlarmClock.EXTRA_LENGTH` accepts: a day. */
    const val MAX_TIMER_SECONDS = 24 * 60 * 60

    private const val SECONDS_PER_MINUTE = 60
    private const val SECONDS_PER_HOUR = 3600
    private const val HOURS_PER_HALF_DAY = 12
    private const val HOURS_PER_DAY = 24
    private const val MINUTES_PER_HOUR = 60

    private val timerPrefixes = listOf(
        "set a timer for", "set timer for", "set a timer", "set timer", "timer for", "timer",
        "mets un minuteur de", "mets un minuteur", "lance un minuteur de", "lance un minuteur",
        "minuteur de", "minuteur", "compte a rebours de", "compte a rebours",
    )
    private val timerSuffixes = listOf("timer", "minuteur")
    private val alarmPrefixes = listOf(
        "set an alarm for", "set alarm for", "set an alarm at", "set alarm at", "set an alarm", "set alarm",
        "alarm for", "alarm at", "alarm",
        "wake me up at", "wake me at", "wake up at", "wake up", "wake at", "wake",
        "mets une alarme a", "mets une alarme pour", "mets une alarme", "mets un reveil a", "mets un reveil",
        "alarme a", "alarme pour", "alarme", "reveille-moi a", "reveille moi a", "reveille-moi", "reveille moi",
        "reveil a", "reveil pour", "reveil",
    )
    private val alarmSuffixes = listOf("alarm", "alarme", "reveil")

    /** One number and its unit; a bare number takes the next smaller unit ("1h30" is 1 h 30 min). */
    private val durationPart = Regex("""(\d+(?:[.,]\d+)?)\s*([a-z]+)?\s*(?:(?:and|et)\s+)?""")

    /** `7`, `7:30`, `7.30`, `7h30`, `7 h 30`, `7h`, with an optional `am`/`pm`. */
    private val timeOfDay = Regex("""(\d{1,2})(?:\s*[:.h]\s*(\d{2})|\s*h)?\s*(am|pm|a\.m\.|p\.m\.|a|p)?""")

    /** The timer's length in seconds, or null when [input] is not a timer. */
    fun timerSeconds(input: String): Int? {
        val text = normalized(input)
        val rest = withoutKeyword(text, timerPrefixes, timerSuffixes)
        val keyword = rest != null
        return duration(rest ?: text, keyword)?.takeIf { it in 1..MAX_TIMER_SECONDS }
    }

    /** The alarm's time, or null when [input] is not an alarm. */
    fun alarmTime(input: String): AlarmTime? {
        val match = withoutKeyword(normalized(input), alarmPrefixes, alarmSuffixes)
            ?.let(timeOfDay::matchEntire) ?: return null
        val (hourText, minuteText, meridiem) = match.destructured
        val minute = minuteText.ifEmpty { "0" }.toInt()
        return hourOfDay(hourText.toInt(), meridiem.firstOrNull())
            ?.takeIf { minute < MINUTES_PER_HOUR }
            ?.let { AlarmTime(it, minute) }
    }

    /** [meridiem] is `a`, `p` or null for the 24-hour clock. */
    private fun hourOfDay(hour: Int, meridiem: Char?): Int? = when (meridiem) {
        null -> hour.takeIf { it < HOURS_PER_DAY }
        else -> if (hour !in 1..HOURS_PER_HALF_DAY) {
            null
        } else {
            hour % HOURS_PER_HALF_DAY + if (meridiem == 'p') HOURS_PER_HALF_DAY else 0
        }
    }

    private fun normalized(input: String) = withoutAccents(input.trim().lowercase()).replace(Regex("""\s+"""), " ")

    /** [text] without its leading keyword or one of its trailing [suffixes]; null when it has neither. */
    private fun withoutKeyword(text: String, prefixes: List<String>, suffixes: List<String>): String? {
        prefixes.firstOrNull { text.startsWith("$it ") }?.let { return text.removePrefix("$it ").trim() }
        val suffix = suffixes.firstOrNull { text.endsWith(" $it") } ?: return null
        return text.removeSuffix(" $suffix").trim()
    }

    /**
     * Seconds in a duration like "1h30", "1 hour and 30 minutes" or "90s". Units must go from larger
     * to smaller, each at most once. A lone bare number means minutes, and only with a [keyword].
     */
    private fun duration(text: String, keyword: Boolean): Int? {
        // Back to back from the start, the parts must cover the whole text.
        val parts = durationPart.findAll(text).toList()
            .takeIf { found -> found.isNotEmpty() && found.sumOf { it.value.length } == text.length }
            ?.takeIf { found -> keyword || found.any { it.groupValues[2].length > 1 } }
        val units = parts?.let { found -> unitsOf(found.map { it.groupValues[2] }, keyword) }
        return if (parts == null || units == null) {
            null
        } else {
            parts.zip(units).sumOf { (part, unit) -> part.groupValues[1].replace(',', '.').toDouble() * unit }
                .roundToInt()
        }
    }

    /** Seconds per unit of each part, from its unit's [names] (empty for a bare number); null if any is wrong. */
    private fun unitsOf(names: List<String>, keyword: Boolean): List<Int>? {
        val units = mutableListOf<Int>()
        for (name in names) {
            val previous = units.lastOrNull()
            val unit = when {
                name.isNotEmpty() -> unitSeconds(name)
                previous != null -> smallerUnit(previous)
                keyword && names.size == 1 -> SECONDS_PER_MINUTE
                else -> null
            }
            if (unit == null || (previous != null && unit >= previous)) return null
            units += unit
        }
        return units
    }

    private fun unitSeconds(unit: String): Int? = when (unit) {
        "h", "hr", "hrs", "hour", "hours", "heure", "heures" -> SECONDS_PER_HOUR
        "m", "min", "mins", "minute", "minutes" -> SECONDS_PER_MINUTE
        "s", "sec", "secs", "second", "seconds", "seconde", "secondes" -> 1
        else -> null
    }

    private fun smallerUnit(unit: Int): Int? = when (unit) {
        SECONDS_PER_HOUR -> SECONDS_PER_MINUTE
        SECONDS_PER_MINUTE -> 1
        else -> null
    }
}
