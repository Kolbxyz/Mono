package com.wander.mono.ui.home.media

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerFoldingTest {
    @Test
    fun `a player opened over a strip at rest folds back by itself`() {
        assertTrue(PlayerFolding.foldsByItself(expanded = true, restsExpanded = false))
    }

    @Test
    fun `a player resting in full stays as it is`() {
        assertFalse(PlayerFolding.foldsByItself(expanded = true, restsExpanded = true))
        assertFalse(PlayerFolding.foldsByItself(expanded = false, restsExpanded = true))
    }

    @Test
    fun `a strip has nothing to fold`() {
        assertFalse(PlayerFolding.foldsByItself(expanded = false, restsExpanded = false))
    }
}
