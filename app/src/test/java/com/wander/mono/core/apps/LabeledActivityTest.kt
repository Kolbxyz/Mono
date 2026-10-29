package com.wander.mono.core.apps

import org.junit.Assert.assertEquals
import org.junit.Test

class LabeledActivityTest {
    private fun entry(pkg: String, label: String, appName: String, serial: Long = 0) =
        LabeledActivity(LaunchableApp(AppKey(pkg, "$pkg.Main", serial), label, serial == 0L), appName)

    @Test
    fun uniqueLabelsGetNoDetail() {
        val apps = resolveLabelClashes(listOf(entry("a.clock", "Clock", "Clock"), entry("a.mail", "Mail", "Mail")))
        assertEquals(listOf(null, null), apps.map { it.detail })
    }

    @Test
    fun clashingLabelsGetTheAppNameWhenItDiffers() {
        val apps = resolveLabelClashes(
            listOf(
                entry("app.grapheneos.camera", "Camera", "Camera"),
                entry("com.google.camera", "Camera", "Pixel Camera"),
            ),
        )
        assertEquals(listOf("app.grapheneos.camera", "Pixel Camera"), apps.map { it.detail })
    }

    @Test
    fun clashingLabelsFallBackToThePackageWhenAppNamesMatch() {
        val apps = resolveLabelClashes(listOf(entry("a.cam", "Camera", "Camera"), entry("b.cam", "Camera", "Camera")))
        assertEquals(listOf("a.cam", "b.cam"), apps.map { it.detail })
    }

    @Test
    fun theSameAppInAnotherProfileIsNotAClash() {
        val work = entry("a.cam", "Camera", "Camera", serial = 10)
        val apps = resolveLabelClashes(listOf(entry("a.cam", "Camera", "Camera"), work))
        assertEquals(listOf(null, null), apps.map { it.detail })
    }
}
