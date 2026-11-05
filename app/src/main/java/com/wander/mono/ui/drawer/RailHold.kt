package com.wander.mono.ui.drawer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * The letter a finger holds on the rail, for the list to keep only that letter's apps in view while
 * the rest step back. [entry] is the rail entry under the finger, -1 while none is held; ★ and the
 * private space's lock hold no letter, so the whole list stays.
 */
@Stable
internal class RailHold {
    var entry by mutableIntStateOf(-1)

    /** The list group the held letter stands for, with [lead] groups before the letters; -1 for none. */
    fun heldGroup(lead: Int, letters: Int): Int =
        if (entry in 1..letters) RailEntries.listItemFor(entry, lead) else -1

    /** Whether the group at [index] shows: all of them unless a letter is held, then only its own. */
    fun shows(index: Int, lead: Int, letters: Int): Boolean {
        val held = heldGroup(lead, letters)
        return held < 0 || held == index
    }
}

/** Fades what does not belong to the held letter out, and back in on release. */
@Composable
internal fun Modifier.heldFade(shown: Boolean): Modifier {
    val alpha by animateFloatAsState(if (shown) 1f else 0f, MaterialTheme.motionScheme.fastEffectsSpec())
    return graphicsLayer { this.alpha = alpha }
}
