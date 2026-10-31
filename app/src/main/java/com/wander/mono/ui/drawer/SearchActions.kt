package com.wander.mono.ui.drawer

/**
 * What the query asks for rather than names: a sum, a unit conversion, a timer, an alarm, a note or
 * a number to call ([CommandQuery]; a contact to call needs the contacts, so [SearchResults] finds
 * it). They read different shapes of query, so at most one usually answers.
 */
internal object SearchActions {
    fun of(query: String): List<SearchHit> = listOfNotNull(
        Calculator.evaluate(query)?.let { SearchHit.Calculation(it) },
        UnitConverter.convert(query)?.let { SearchHit.Calculation(it) },
        ClockQuery.timerSeconds(query)?.let { SearchHit.Timer(it) },
        ClockQuery.alarmTime(query)?.let { SearchHit.Alarm(it) },
        CommandQuery.note(query)?.let { SearchHit.Note(it) },
        CommandQuery.callee(query)?.let(CommandQuery::number)?.let { SearchHit.Dial(it) },
    )
}
