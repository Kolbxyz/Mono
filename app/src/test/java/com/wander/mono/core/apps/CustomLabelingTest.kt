package com.wander.mono.core.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class CustomLabelingTest {
    private val camera = LaunchableApp(AppKey("org.camera", "org.camera.Main", 0), "Camera", true, detail = "GrapheneOS")
    private val clock = LaunchableApp(AppKey("com.clock", "com.clock.Main", 0), "Clock", true)

    @Test
    fun `renamed apps take the user's name and keep their own`() {
        val result = withCustomLabels(listOf(camera, clock), mapOf(camera.key to "Photos"))
        assertEquals(camera.copy(label = "Photos", detail = null, systemLabel = "Camera"), result[0])
        assertSame(clock, result[1])
    }

    @Test
    fun `a label equal to the app's own name changes nothing`() {
        assertSame(clock, withCustomLabels(listOf(clock), mapOf(clock.key to "Clock")).single())
    }
}
