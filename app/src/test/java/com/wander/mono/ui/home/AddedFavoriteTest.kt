package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddedFavoriteTest {
    private val wanda = AppKey("com.wander.android", "Main", 0)
    private val phalanx = AppKey("org.phalanx.android", "Main", 0)
    private val clock = AppKey("com.android.deskclock", "Main", 0)

    @Test
    fun `one new key is the favourite just added`() {
        assertEquals(clock, addedFavorite(listOf(wanda, phalanx), listOf(wanda, phalanx, clock)))
    }

    @Test
    fun `reorders and removals add nothing`() {
        assertNull(addedFavorite(listOf(wanda, phalanx), listOf(phalanx, wanda)))
        assertNull(addedFavorite(listOf(wanda, phalanx), listOf(wanda)))
    }

    @Test
    fun `several keys at once, as a restore brings, add nothing to point out`() {
        assertNull(addedFavorite(listOf(wanda), listOf(wanda, phalanx, clock)))
    }
}
