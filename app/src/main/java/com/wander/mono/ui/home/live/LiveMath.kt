package com.wander.mono.ui.home.live

import kotlin.math.floor

/**
 * Whole seconds a chronometer shows at [now]: counted up from [base], or down to it when
 * [countsDown] (negative once a countdown has passed it). Truncated towards zero, as the platform's
 * `Chronometer` does, so home and the shade show the same second.
 */
internal fun chronometerSeconds(base: Long, now: Long, countsDown: Boolean): Long =
    (if (countsDown) base - now else now - base) / MS_PER_SECOND

/** Milliseconds from [now] until a chronometer based at [base] shows its next second, 1 to 1000. */
internal fun msUntilNextSecond(base: Long, now: Long): Long = MS_PER_SECOND - Math.floorMod(now - base, MS_PER_SECOND)

/**
 * [fraction] as a whole percentage, rounded down so 100 % only shows once done. The slack absorbs
 * float error, so 29 of 100 (0.29f, 28.999… %) still reads 29.
 */
internal fun percentOf(fraction: Float): Int = floor(fraction.coerceIn(0f, 1f) * PERCENT + FLOAT_SLACK).toInt()

private const val MS_PER_SECOND = 1_000L
private const val PERCENT = 100f
private const val FLOAT_SLACK = 0.001f
