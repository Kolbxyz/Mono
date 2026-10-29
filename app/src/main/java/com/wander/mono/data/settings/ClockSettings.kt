package com.wander.mono.data.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * The bundled clock faces (all OFL, licences in `res/raw`). [weights] is the range the font's
 * `wght` axis covers; a static font has a single weight and no weight slider.
 */
enum class ClockFont(override val stored: String, val weights: IntRange) : StoredEnum {
    Flex("flex", weights = 100..1000),
    Rounded("rounded", weights = 200..1000),
    Serif("serif", weights = 400..400),
    Mono("mono", weights = 100..800),
    Condensed("condensed", weights = 200..700),
    ;

    val isVariable: Boolean get() = weights.first != weights.last
}

/** Default: one big line. Stacked: hours over minutes. Small: one modest line, more room for the list. */
enum class ClockStyle(override val stored: String) : StoredEnum {
    Default("default"),
    Stacked("stacked"),
    Small("small"),
}

enum class ClockAlignment(override val stored: String) : StoredEnum {
    Start("start"),
    Center("center"),
}

/** The date line, as ICU skeletons: the locale decides order and separators. */
enum class DateStyle(override val stored: String, val skeleton: String) : StoredEnum {
    Full("full", "EEEEMMMMd"),
    Short("short", "EEEMMMd"),
    Numeric("numeric", "EEEdM"),
    Weekday("weekday", "EEEE"),
}

object ClockSettings {
    val Font = EnumSetting("clock_font", ClockFont.Flex, ClockFont.entries)

    /** The `wght` axis value, in hundreds so the slider has clean stops. */
    val Weight = IntSetting("clock_weight", default = 800, range = 100..1000)

    /** The clock's size in sp, for the Default and Stacked styles. */
    val Size = IntSetting("clock_size", default = 88, range = CLOCK_SIZES)

    val Style = EnumSetting("clock_style", ClockStyle.Default, ClockStyle.entries)

    val Alignment = EnumSetting("clock_alignment", ClockAlignment.Start, ClockAlignment.entries)

    val Date = EnumSetting("clock_date", DateStyle.Full, DateStyle.entries)

    val ShowDate = BooleanSetting("clock_show_date", default = true)

    /** The next alarm, only when it rings within a day. */
    val ShowAlarm = BooleanSetting("clock_show_alarm", default = true)

    /** The next calendar event, when calendar access is on and home has no calendar row. */
    val ShowEvent = BooleanSetting("clock_show_event", default = true)

    /** A battery chip, only when low or charging. */
    val ShowBattery = BooleanSetting("clock_show_battery", default = true)
}

/** Clock sizes the slider stops at, in sp. */
val CLOCK_SIZES = 56..120

/** Distance between the size slider's stops, in sp. */
const val CLOCK_SIZE_STEP = 8

/** Distance between the weight slider's stops. */
const val CLOCK_WEIGHT_STEP = 100

/** Which lines the glance header shows under the time. */
data class GlanceLines(
    val date: Boolean = true,
    val alarm: Boolean = true,
    val event: Boolean = true,
    val battery: Boolean = true,
)

/** Everything that shapes the clock, read together so the header changes in one step. */
data class ClockLook(
    val font: ClockFont = ClockFont.Flex,
    val weight: Int = ClockSettings.Weight.default,
    val size: Int = ClockSettings.Size.default,
    val style: ClockStyle = ClockStyle.Default,
    val alignment: ClockAlignment = ClockAlignment.Start,
    val date: DateStyle = DateStyle.Full,
    val lines: GlanceLines = GlanceLines(),
)

fun SettingsStore.clockLook(): Flow<ClockLook> {
    val face = combine(
        this[ClockSettings.Font],
        this[ClockSettings.Weight],
        this[ClockSettings.Size],
        this[ClockSettings.Style],
        this[ClockSettings.Alignment],
    ) { font, weight, size, style, alignment -> ClockLook(font, weight, size, style, alignment) }
    val lines = combine(
        this[ClockSettings.ShowDate],
        this[ClockSettings.ShowAlarm],
        this[ClockSettings.ShowEvent],
        this[ClockSettings.ShowBattery],
        ::GlanceLines,
    )
    return combine(face, this[ClockSettings.Date], lines) { look, date, shown ->
        look.copy(date = date, lines = shown)
    }
}
