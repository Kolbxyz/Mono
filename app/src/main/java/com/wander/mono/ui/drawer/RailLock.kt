package com.wander.mono.ui.drawer

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.unit.dp
import com.wander.mono.R

/** The private space at the letter rail's end: whether it is [locked], and locking or unlocking it. */
class RailLock(val locked: Boolean, val onToggle: () -> Unit) {
    /** The state it is in, as the private space's own card shows it. */
    @get:DrawableRes
    val symbol: Int get() = if (locked) R.drawable.sym_lock else R.drawable.sym_lock_open
}

/** The lock in the rail's last slot: tertiary like the private badge, primary while under the finger. */
@Composable
internal fun RailLockGlyph(lock: RailLock, lit: Boolean, modifier: Modifier = Modifier) {
    Icon(
        painterResource(lock.symbol),
        contentDescription = null,
        tint = if (lit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
        modifier = modifier.size(LOCK_SIZE),
    )
}

/** What TalkBack reads for each rail entry: ★ in words, the letters, and the lock as the private space. */
internal fun railSpoken(entries: List<String>, top: String, private: String, hasLock: Boolean): List<String> =
    entries.mapIndexed { i, entry ->
        when {
            i == 0 -> top
            hasLock && i == entries.lastIndex -> private
            else -> entry
        }
    }

/** Locking or unlocking the private space, as a TalkBack action on the rail; none without a [lock]. */
@Composable
internal fun rememberLockActions(lock: RailLock?): List<CustomAccessibilityAction> {
    if (lock == null) return emptyList()
    val name = stringResource(if (lock.locked) R.string.drawer_private_unlock else R.string.drawer_private_lock)
    return remember(name, lock) {
        listOf(
            CustomAccessibilityAction(name) {
                lock.onToggle()
                true
            },
        )
    }
}

private val LOCK_SIZE = 14.dp
