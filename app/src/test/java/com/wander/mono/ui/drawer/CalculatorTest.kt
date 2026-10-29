package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculatorTest {
    private fun eval(input: String) = Calculator.evaluate(input)

    @Test
    fun `basic operations`() {
        assertEquals("5", eval("2+3"))
        assertEquals("-1", eval("2-3"))
        assertEquals("6", eval("2*3"))
        assertEquals("0.5", eval("1/2"))
        assertEquals("8", eval("2^3"))
        assertEquals("1", eval("10 % 3"))
    }

    @Test
    fun `precedence and associativity`() {
        assertEquals("14", eval("2+3*4"))
        assertEquals("20", eval("(2+3)*4"))
        assertEquals("2", eval("8/2/2"))
        assertEquals("1", eval("5-2-2"))
        assertEquals("512", eval("2^3^2"))
        assertEquals("-4", eval("-2^2"))
        assertEquals("0.5", eval("2^-1"))
        assertEquals("19", eval("1+2*3^2"))
        assertEquals("7", eval("1 + 6 % 4 * 3"))
    }

    @Test
    fun `unary signs and nesting`() {
        assertEquals("-6", eval("-(2+4)"))
        assertEquals("5", eval("2--3"))
        assertEquals("9", eval("((((1+2))))*3"))
    }

    @Test
    fun `percent after an operand is a percentage`() {
        assertEquals("0.5", eval("50%"))
        assertEquals("20", eval("200*10%"))
    }

    @Test
    fun `both decimal separators and the result uses the one typed`() {
        assertEquals("3.5", eval("1.5+2"))
        assertEquals("3,5", eval("1,5+2"))
        assertEquals("0.3", eval("0.1+0.2"))
        assertEquals("0.75", eval(".5+.25"))
    }

    @Test
    fun `typographic operators`() {
        assertEquals("6", eval("2×3"))
        assertEquals("2", eval("6÷3"))
        assertEquals("1", eval("3−2"))
    }

    @Test
    fun `division by zero gives no result`() {
        assertNull(eval("1/0"))
        assertNull(eval("1/(2-2)"))
        assertNull(eval("5 % 0"))
        assertNull(eval("0/0"))
    }

    @Test
    fun `overflow gives no result`() {
        assertNull(eval("10^400"))
        assertNull(eval("10^400-10^400"))
    }

    @Test
    fun `malformed input gives no result`() {
        listOf("1+", "*2", "(1+2", "1+2)", "()", "1..2+1", "1.2.3+1", "2(3)", "1 2", "abc", "1+a", "", "  ", "-", "%")
            .forEach { assertNull("'$it'", eval(it)) }
    }

    @Test
    fun `a lone number is not a calculation`() {
        listOf("42", "-5", "+5", "(7)", "3,5", "2048").forEach { assertNull("'$it'", eval(it)) }
    }

    @Test
    fun `results are rounded and stay readable`() {
        assertEquals("0.333333333333", eval("1/3"))
        assertEquals("1000000", eval("1000*1000"))
        assertEquals("1E+20", eval("10^20"))
    }
}
