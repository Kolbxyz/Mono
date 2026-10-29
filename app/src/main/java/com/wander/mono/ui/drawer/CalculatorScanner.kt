package com.wander.mono.ui.drawer

/** Thrown while reading a calculation only, so one bad token unwinds the whole descent. */
internal class MalformedExpression : RuntimeException()

/**
 * The characters of a calculation for [Calculator]'s parser: skips spaces, reads operators (with
 * `× ÷ −` read as `* / -`) and numbers (with `.` or `,` as the decimal mark).
 */
internal class CalculatorScanner(private val text: String) {
    private var pos = 0

    /** True once a number was written with `,`, so the result can use it too. */
    var decimalComma = false
        private set

    val atEnd: Boolean
        get() {
            skipSpaces()
            return pos >= text.length
        }

    /** The next operator, or null when anything else comes next. Does not consume it. */
    fun peekOperator(): Char? {
        skipSpaces()
        if (pos >= text.length) return null
        return when (val c = text[pos]) {
            '+', '-', '*', '/', '^', '%' -> c
            '×' -> '*'
            '÷' -> '/'
            '−' -> '-'
            else -> null
        }
    }

    /** Consumes [c] when it comes next; false, consuming nothing, otherwise. */
    fun take(c: Char): Boolean {
        skipSpaces()
        if (pos >= text.length || text[pos] != c) return false
        pos++
        return true
    }

    /** Consumes the operator [peekOperator] returned. */
    fun skip() {
        pos++
    }

    /** Whether the `%` about to be read has an operand after it: a remainder, not a percentage. */
    fun operandFollowsPercent(): Boolean {
        var next = pos + 1
        while (next < text.length && text[next].isWhitespace()) next++
        return next < text.length && (text[next].isAsciiDigit() || text[next] in "(.,")
    }

    fun number(): Double {
        skipSpaces()
        val start = pos
        while (pos < text.length && (text[pos].isAsciiDigit() || text[pos] in ".,")) pos++
        val token = text.substring(start, pos)
        if (token.count { it in ".," } > 1 || token.none { it.isAsciiDigit() }) throw MalformedExpression()
        if (',' in token) decimalComma = true
        return token.replace(',', '.').toDouble()
    }

    private fun skipSpaces() {
        while (pos < text.length && text[pos].isWhitespace()) pos++
    }

    private fun Char.isAsciiDigit() = this in '0'..'9'
}
