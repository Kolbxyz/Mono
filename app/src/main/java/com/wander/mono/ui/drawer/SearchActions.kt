package com.wander.mono.ui.drawer

/**
 * What the query asks for rather than names: a sum, a unit conversion, a timer or an alarm. They
 * read different shapes of query, so at most one usually answers.
 */
internal object SearchActions {
    fun of(query: String): List<SearchHit> = listOfNotNull(
        Calculator.evaluate(query)?.let { SearchHit.Calculation(it) },
        UnitConverter.convert(query)?.let { SearchHit.Calculation(it) },
        ClockQuery.timerSeconds(query)?.let { SearchHit.Timer(it) },
        ClockQuery.alarmTime(query)?.let { SearchHit.Alarm(it) },
    )
}
