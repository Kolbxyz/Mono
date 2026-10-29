package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UnitConverterTest {
    private fun convert(query: String) = UnitConverter.convert(query)

    @Test
    fun `lengths, with any connector`() {
        assertEquals("3.10686 mi", convert("5 km in mi"))
        assertEquals("3.10686 mi", convert("5km to miles"))
        assertEquals("3.10686 mi", convert("5 kilometres = mi"))
        assertEquals("3.10686 mi", convert("5 km -> mi"))
        assertEquals("12.7 cm", convert("5 in in cm"))
        assertEquals("6 ft", convert("72 inches in feet"))
        assertEquals("1.852 km", convert("1 nautical mile in km"))
    }

    @Test
    fun `temperatures, including below zero`() {
        assertEquals("21.1111 °C", convert("70f to c"))
        assertEquals("212 °F", convert("100 °C in °F"))
        assertEquals("-40 °F", convert("-40 c in f"))
        assertEquals("273.15 K", convert("0 celsius in kelvin"))
    }

    @Test
    fun `mass, volume, speed and area`() {
        assertEquals("2.20462 lb", convert("1 kg in lbs"))
        assertEquals("709.765 ml", convert("3 cups in ml"))
        assertEquals("29.5735 ml", convert("1 fl oz in ml"))
        assertEquals("62.1371 mph", convert("100 km/h in mph"))
        assertEquals("10.7639 ft²", convert("1 m² in sq ft"))
        assertEquals("2.47105 ac", convert("1 hectare in acres"))
        assertEquals("1000000 m²", convert("1 km2 in square meters"))
    }

    @Test
    fun `a decimal comma is read and kept`() {
        assertEquals("2,54 cm", convert("1,0 in in cm"))
        assertEquals("4,02336 km", convert("2,5 mi in km"))
    }

    @Test
    fun `units must measure the same thing`() {
        assertNull(convert("5 km in kg"))
        assertNull(convert("5 c in ml"))
    }

    @Test
    fun `not conversions`() {
        assertNull(convert("5 km"))
        assertNull(convert("km in mi"))
        assertNull(convert("5 parsecs in km"))
        assertNull(convert("1.2.3 km in mi"))
        assertNull(convert("photos in drive"))
        assertNull(convert("2+2"))
        assertNull(convert("timer 10m"))
    }

    @Test
    fun `units are found by plural and in any case`() {
        assertEquals("cup", Units.find("Cups")?.symbol)
        assertEquals("°C", Units.find("°C")?.symbol)
        assertEquals("m", Units.find("Metres")?.symbol)
        assertNull(Units.find("ms"))
    }
}
