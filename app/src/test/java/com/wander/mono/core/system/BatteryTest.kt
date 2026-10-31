package com.wander.mono.core.system

import android.os.BatteryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryTest {
    @Test
    fun `level is scaled to percent`() {
        assertEquals(BatteryState(50, charging = false), batteryState(100, 200, DISCHARGING, UNPLUGGED))
    }

    @Test
    fun `charging needs a charger and a charging or full status`() {
        assertTrue(batteryState(40, 100, BatteryManager.BATTERY_STATUS_CHARGING, AC)!!.charging)
        assertTrue(batteryState(100, 100, BatteryManager.BATTERY_STATUS_FULL, AC)!!.charging)
        assertFalse(batteryState(40, 100, BatteryManager.BATTERY_STATUS_CHARGING, UNPLUGGED)!!.charging)
        assertFalse(batteryState(40, 100, DISCHARGING, AC)!!.charging)
    }

    @Test
    fun `no level means no battery`() {
        assertNull(batteryState(-1, 100, DISCHARGING, UNPLUGGED))
        assertNull(batteryState(50, 0, DISCHARGING, UNPLUGGED))
    }

    @Test
    fun `the chip shows at fifteen percent and below, or while charging`() {
        assertTrue(BatteryState(15, charging = false).isWorthShowing())
        assertTrue(BatteryState(3, charging = false).isWorthShowing())
        assertFalse(BatteryState(16, charging = false).isWorthShowing())
        assertTrue(BatteryState(80, charging = true).isWorthShowing())
        assertTrue(BatteryState(80, charging = false, saver = true).isWorthShowing())
    }

    private companion object {
        const val DISCHARGING = BatteryManager.BATTERY_STATUS_DISCHARGING
        const val UNPLUGGED = 0
        const val AC = BatteryManager.BATTERY_PLUGGED_AC
    }
}
