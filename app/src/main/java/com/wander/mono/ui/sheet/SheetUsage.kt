package com.wander.mono.ui.sheet

/**
 * Today's use of the sheet's app as its header shows it. [Reading] keeps the line's height while
 * the time is read, so the header does not grow under the opening sheet.
 */
sealed interface SheetUsage {
    /** Off in settings, without usage access, or an app of another profile. */
    data object Hidden : SheetUsage

    data object Reading : SheetUsage

    data class Today(val millis: Long) : SheetUsage
}

/** What the header shows before reading starts: [Reading] only for an app whose time can be read. */
internal fun usageBeforeReading(show: Boolean, readable: Boolean): SheetUsage =
    if (show && readable) SheetUsage.Reading else SheetUsage.Hidden

/** What a finished read shows: null means no access after all. */
internal fun usageRead(millis: Long?): SheetUsage = millis?.let(SheetUsage::Today) ?: SheetUsage.Hidden
