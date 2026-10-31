package com.wander.mono.ui.theme

import java.util.Calendar

/**
 * Where the day is, as the clock wears it: a little bolder at midday, lighter into the evening and
 * lightest at night, when it also dims a touch. [weightShift] is added to the user's weight on the
 * font's `wght` axis; [brightness] is the clock's opacity. Only a handful of boundaries, so the clock
 * changes a few times a day, never every minute.
 */
internal enum class DayPhase(val weightShift: Int, val brightness: Float) {
    Night(weightShift = -110, brightness = 0.85f),
    Morning(weightShift = 0, brightness = 1f),
    Midday(weightShift = 60, brightness = 1f),
    Evening(weightShift = -30, brightness = 1f),
    Late(weightShift = -70, brightness = 0.92f),
}

/** The phase for [hour] of the day, 0 to 23. */
internal fun dayPhase(hour: Int): DayPhase = when (hour) {
    in MORNING_FROM until MIDDAY_FROM -> DayPhase.Morning
    in MIDDAY_FROM until EVENING_FROM -> DayPhase.Midday
    in EVENING_FROM until LATE_FROM -> DayPhase.Evening
    in LATE_FROM until NIGHT_FROM -> DayPhase.Late
    else -> DayPhase.Night
}

/** The phase at [millis] in the device's zone (the tick that carries it also fires on a zone change). */
internal fun dayPhaseAt(millis: Long): DayPhase =
    dayPhase(Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.HOUR_OF_DAY))

/** [weight] shifted for [phase], kept on the font's axis ([axis]); a static font stays where it is. */
internal fun dayWeight(weight: Int, phase: DayPhase, axis: IntRange): Int =
    (weight + phase.weightShift).coerceIn(axis)

/**
 * A weight (or a shift of one) as drawn while the clock eases to a new one: in [WEIGHT_STEP] steps, so the
 * ease builds a few font instances rather than one per weight unit.
 */
internal fun steppedWeight(weight: Int): Int = weight / WEIGHT_STEP * WEIGHT_STEP

private const val MORNING_FROM = 6
private const val MIDDAY_FROM = 11
private const val EVENING_FROM = 17
private const val LATE_FROM = 21
private const val NIGHT_FROM = 24

private const val WEIGHT_STEP = 10
