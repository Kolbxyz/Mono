package com.wander.mono.data.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class RowDensityTest {
    @Test
    fun `next cycles through every density and back`() {
        assertEquals(RowDensity.Comfortable, RowDensity.Default.next())
        assertEquals(RowDensity.Compact, RowDensity.Comfortable.next())
        assertEquals(RowDensity.Default, RowDensity.Compact.next())
    }

    @Test
    fun `an unknown stored density reads as the default`() {
        assertEquals(RowDensity.Default, HomeLookSettings.Density.decode("roomy"))
        assertEquals(RowDensity.Compact, HomeLookSettings.Density.decode("compact"))
    }
}
