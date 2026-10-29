package com.wander.mono.ui.drawer

/**
 * Reads a search query as a unit conversion, offline: "5 km in mi", "70f to c", "3 cups = ml".
 * The amount is a number written like the calculator reads one (`.` or `,` as the decimal mark,
 * kept in the result), optionally negative; the two units must measure the same thing.
 */
internal object UnitConverter {
    /** Fewer digits than the calculator: unit factors are themselves rounded, and 3.10686 mi reads better. */
    private const val SIGNIFICANT_DIGITS = 6
    private const val MAX_LENGTH = 100
    private const val MINUS_SIGNS = "-−"

    /** Amount, source unit, connector, target unit; the lazy source unit splits "5 in in cm" right. */
    private val conversion =
        Regex("""([-−]?\s*[\d.,]+)\s*(.+?)(?:\s+(?:in|to|into|as)\s+|\s*(?:=|->|→)\s*)(.+)""")

    /** The converted amount with its unit's symbol ("3.10686 mi"), or null when [input] is not a conversion. */
    fun convert(input: String): String? {
        val text = input.trim().lowercase()
        val match = text.takeIf { it.length <= MAX_LENGTH }?.let(conversion::matchEntire) ?: return null
        val (amountText, fromName, toName) = match.destructured
        val units = Units.find(fromName)?.let { from ->
            Units.find(toName)?.takeIf { it.quantity == from.quantity }?.let { target -> from to target }
        }
        val scanner = CalculatorScanner(amountText.trimStart(*MINUS_SIGNS.toCharArray()).trim())
        val amount = number(scanner)?.let { if (amountText.first() in MINUS_SIGNS) -it else it }
        return if (units == null || amount == null) {
            null
        } else {
            val (from, to) = units
            val value = to.fromBase(from.toBase(amount))
            "${Calculator.format(value, scanner.decimalComma, SIGNIFICANT_DIGITS)} ${to.symbol}"
        }
    }

    /** The one number [scanner] holds, or null when it holds anything else ("1.2.3"). */
    private fun number(scanner: CalculatorScanner): Double? = try {
        scanner.number().takeIf { scanner.atEnd }
    } catch (_: MalformedExpression) {
        null
    }
}
