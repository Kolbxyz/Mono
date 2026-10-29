package com.wander.mono.ui.drawer

/** What a unit measures; only units of the same kind convert into each other. */
internal enum class Quantity { Length, Mass, Temperature, Volume, Speed, Area }

/**
 * A unit as `base = value * factor + offset`, where the base is the SI unit of its [quantity]
 * (metre, kilogram, kelvin, litre, metre per second, square metre). [symbol] is how results show it;
 * [names] are what people type for it, lowercase.
 */
internal class QuantityUnit(
    val symbol: String,
    val quantity: Quantity,
    val factor: Double,
    val names: List<String>,
    val offset: Double = 0.0,
) {
    fun toBase(value: Double) = value * factor + offset

    fun fromBase(value: Double) = (value - offset) / factor
}

/**
 * The units search converts between, by their symbols and English names. Volumes are US customary:
 * a cup is 236.6 ml, a gallon 3.785 l.
 */
internal object Units {
    private const val PLURAL_MIN_LENGTH = 4
    private const val CELSIUS_OFFSET = 273.15
    private const val FAHRENHEIT_FACTOR = 5.0 / 9.0
    private const val FAHRENHEIT_OFFSET = 459.67 * FAHRENHEIT_FACTOR
    private const val NAUTICAL_MILE = "nmi"

    private val all: List<QuantityUnit> = lengths() + areas() + masses() + temperatures() + volumes() + speeds()

    private val byName: Map<String, QuantityUnit> = all.flatMap { unit -> unit.names.map { it to unit } }.toMap()

    init {
        require(byName.size == all.sumOf { it.names.size }) { "Two units share a name" }
    }

    /** The unit called [name] (any case), also in a regular plural ("cups", "pounds"). */
    fun find(name: String): QuantityUnit? {
        val key = name.trim().lowercase().replace(Regex("""\s+"""), " ")
        return byName[key] ?: key.takeIf { it.length >= PLURAL_MIN_LENGTH && it.endsWith('s') }
            ?.let { byName[it.dropLast(1)] }
    }

    private fun unit(symbol: String, quantity: Quantity, factor: Double, vararg words: String) =
        QuantityUnit(symbol, quantity, factor = factor, names = listOf(symbol.lowercase(), *words))

    private fun lengths() = listOf(
        unit("mm", Quantity.Length, factor = 0.001, "millimeter", "millimetre", "millimeters", "millimetres"),
        unit("cm", Quantity.Length, factor = 0.01, "centimeter", "centimetre", "centimeters", "centimetres"),
        unit("m", Quantity.Length, factor = 1.0, "meter", "metre", "meters", "metres"),
        unit("km", Quantity.Length, factor = 1000.0, "kilometer", "kilometre", "kilometers", "kilometres"),
        unit("in", Quantity.Length, factor = 0.0254, "inch", "inches", "\""),
        unit("ft", Quantity.Length, factor = 0.3048, "foot", "feet", "'"),
        unit("yd", Quantity.Length, factor = 0.9144, "yard", "yards"),
        unit("mi", Quantity.Length, factor = 1609.344, "mile", "miles"),
        unit(NAUTICAL_MILE, Quantity.Length, factor = 1852.0, "nautical mile", "nautical miles"),
    )

    /** The square of each length but the nautical mile: "m²", "m2", "sq m", "square metres". */
    private fun areas() = lengths().filter { it.symbol != NAUTICAL_MILE }.map { side ->
        val sides = side.names.filter { it.first().isLetter() }
        QuantityUnit(
            "${side.symbol}²",
            Quantity.Area,
            factor = side.factor * side.factor,
            names = sides.flatMap { listOf("$it²", "${it}2", "sq $it", "square $it") },
        )
    } + listOf(
        unit("ha", Quantity.Area, factor = 10_000.0, "hectare"),
        unit("ac", Quantity.Area, factor = 4046.8564224, "acre"),
    )

    private fun masses() = listOf(
        unit("mg", Quantity.Mass, factor = 1e-6, "milligram"),
        unit("g", Quantity.Mass, factor = 0.001, "gram", "gramme"),
        unit("kg", Quantity.Mass, factor = 1.0, "kilo", "kilogram", "kilogramme"),
        unit("t", Quantity.Mass, factor = 1000.0, "tonne", "metric ton"),
        unit("oz", Quantity.Mass, factor = 0.028349523125, "ounce"),
        unit("lb", Quantity.Mass, factor = 0.45359237, "lbs", "pound"),
        unit("st", Quantity.Mass, factor = 6.35029318, "stone"),
    )

    private fun temperatures() = listOf(
        QuantityUnit(
            "°C",
            Quantity.Temperature,
            factor = 1.0,
            offset = CELSIUS_OFFSET,
            names = listOf("c", "°c", "celsius", "degree celsius", "degrees celsius"),
        ),
        QuantityUnit(
            "°F",
            Quantity.Temperature,
            factor = FAHRENHEIT_FACTOR,
            offset = FAHRENHEIT_OFFSET,
            names = listOf("f", "°f", "fahrenheit", "degree fahrenheit", "degrees fahrenheit"),
        ),
        unit("K", Quantity.Temperature, factor = 1.0, "kelvin"),
    )

    private fun volumes() = listOf(
        unit("ml", Quantity.Volume, factor = 0.001, "milliliter", "millilitre"),
        unit("cl", Quantity.Volume, factor = 0.01, "centiliter", "centilitre"),
        unit("dl", Quantity.Volume, factor = 0.1, "deciliter", "decilitre"),
        unit("l", Quantity.Volume, factor = 1.0, "liter", "litre"),
        unit("m³", Quantity.Volume, factor = 1000.0, "m3", "cubic meter", "cubic metre"),
        unit("tsp", Quantity.Volume, factor = 0.00492892159375, "teaspoon"),
        unit("tbsp", Quantity.Volume, factor = 0.01478676478125, "tablespoon"),
        unit("fl oz", Quantity.Volume, factor = 0.0295735295625, "floz", "fluid ounce"),
        unit("cup", Quantity.Volume, factor = 0.2365882365),
        unit("pt", Quantity.Volume, factor = 0.473176473, "pint"),
        unit("qt", Quantity.Volume, factor = 0.946352946, "quart"),
        unit("gal", Quantity.Volume, factor = 3.785411784, "gallon"),
    )

    private fun speeds() = listOf(
        unit(
            "m/s", Quantity.Speed, factor = 1.0,
            "mps", "meter per second", "metre per second", "meters per second", "metres per second",
        ),
        unit(
            "km/h", Quantity.Speed, factor = 1 / 3.6,
            "kmh", "kph", "kmph",
            "kilometer per hour", "kilometre per hour", "kilometers per hour", "kilometres per hour",
        ),
        unit("mph", Quantity.Speed, factor = 0.44704, "mi/h", "mile per hour", "miles per hour"),
        unit("kn", Quantity.Speed, factor = 1852.0 / 3600, "kt", "knot"),
        unit("ft/s", Quantity.Speed, factor = 0.3048, "fps", "foot per second", "feet per second"),
    )
}
