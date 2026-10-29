package com.wander.mono.ui.drawer

import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.pow

/**
 * Evaluates what is typed in search when it is arithmetic: `+ - * / ^ %` (also `× ÷ −`),
 * parentheses, and decimals with either `.` or `,`. `%` between two operands is the remainder,
 * after one it is a percentage (`50%` is 0.5). A query that is only a number is not a calculation.
 * Division by zero, overflow and anything malformed give no result rather than an error.
 *
 * Grammar, by recursive descent, loosest binding first:
 * ```
 * sum     = product (("+" | "-") product)*
 * product = unary (("*" | "/" | "%") unary)*
 * unary   = ("+" | "-") unary | power
 * power   = postfix ("^" unary)?        right-associative; -2^2 is -4
 * postfix = primary "%"*
 * primary = number | "(" sum ")"
 * ```
 */
internal object Calculator {
    private const val MAX_LENGTH = 200
    private const val SIGNIFICANT_DIGITS = 12
    private const val PERCENT = 100.0
    private const val MIN_PLAIN_MAGNITUDE = -6
    private const val MAX_PLAIN_MAGNITUDE = 15

    /** The formatted result, or null when [input] is not a calculation. */
    fun evaluate(input: String): String? {
        val text = input.trim()
        if (text.isEmpty() || text.length > MAX_LENGTH) return null
        val parser = Parser(CalculatorScanner(text))
        return parser.parse()?.takeIf { parser.operated }?.let { format(it, parser.decimalComma) }
    }

    /**
     * At most [significantDigits] significant digits and no trailing zeros; an exponent only for
     * very large or small values. Uses `,` as the decimal mark when the query did.
     */
    fun format(value: Double, decimalComma: Boolean = false, significantDigits: Int = SIGNIFICANT_DIGITS): String {
        val rounded = BigDecimal(value).round(MathContext(significantDigits)).stripTrailingZeros()
        val magnitude = rounded.precision() - rounded.scale() - 1
        val plain = when {
            value == 0.0 -> "0"
            magnitude in MIN_PLAIN_MAGNITUDE..MAX_PLAIN_MAGNITUDE -> rounded.toPlainString()
            else -> rounded.toString()
        }
        return if (decimalComma) plain.replace('.', ',') else plain
    }

    private class Parser(private val scanner: CalculatorScanner) {
        /** True once any operator was applied; a lone number, even signed, is not a calculation. */
        var operated = false
            private set

        val decimalComma: Boolean get() = scanner.decimalComma

        /** Null when malformed, dividing by zero, or too large to show. */
        fun parse(): Double? = try {
            val value = sum()
            if (!scanner.atEnd) throw MalformedExpression()
            value.takeIf { it.isFinite() }
        } catch (_: MalformedExpression) {
            null
        }

        private fun sum(): Double {
            var value = product()
            while (true) {
                value = when (scanner.peekOperator()) {
                    '+' -> applied { value + product() }
                    '-' -> applied { value - product() }
                    else -> return value
                }
            }
        }

        private fun product(): Double {
            var value = unary()
            while (true) {
                value = when (scanner.peekOperator()) {
                    '*' -> applied { value * unary() }
                    '/' -> applied { value / nonZero(unary()) }
                    '%' if scanner.operandFollowsPercent() -> applied { value.rem(nonZero(unary())) }
                    else -> return value
                }
            }
        }

        /** A sign alone is not an operation: `-5` is a number, not a calculation. */
        private fun unary(): Double = when (scanner.peekOperator()) {
            '-' -> -signed()
            '+' -> signed()
            else -> power()
        }

        private fun signed(): Double {
            scanner.skip()
            return unary()
        }

        private fun power(): Double {
            val base = postfix()
            return if (scanner.peekOperator() == '^') applied { base.pow(unary()) } else base
        }

        private fun postfix(): Double {
            var value = primary()
            while (scanner.peekOperator() == '%' && !scanner.operandFollowsPercent()) {
                value = applied { value / PERCENT }
            }
            return value
        }

        private fun primary(): Double {
            if (!scanner.take('(')) return scanner.number()
            val value = sum()
            if (!scanner.take(')')) throw MalformedExpression()
            return value
        }

        /** Consumes the operator just peeked at, then evaluates its operand(s). */
        private inline fun applied(evaluate: () -> Double): Double {
            scanner.skip()
            operated = true
            return evaluate()
        }

        private fun nonZero(value: Double): Double = if (value == 0.0) throw MalformedExpression() else value
    }
}
