package com.wander.mono.core.notifications

import com.wander.mono.core.quiet.QuietMode
import com.wander.mono.di.AppScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * What came in while quiet mode was on: the apps whose notification counts grew during the session
 * and by how much, published once it ends and kept until home has shown it ([clear]). Read off the
 * listener's own [NotificationCounts] as they change; held in memory only, never written anywhere,
 * so a session the process did not live through leaves no digest.
 */
@Singleton
class QuietDigest @Inject constructor(
    counts: NotificationCounts,
    quiet: QuietMode,
    @AppScope scope: CoroutineScope,
) {
    private val mutableAway = MutableStateFlow<Map<PackageUser, Int>>(emptyMap())

    /** Per app, how many notifications came in during the quiet session that just ended; empty otherwise. */
    val away: StateFlow<Map<PackageUser, Int>> = mutableAway.asStateFlow()

    init {
        scope.launch {
            quiet.enabled.distinctUntilChanged().collectLatest { on ->
                if (!on) return@collectLatest
                // A new session starts clean; what it gathers shows once it is switched off.
                mutableAway.value = emptyMap()
                val gathered = HashMap<PackageUser, Int>()
                var before: Map<PackageUser, Int>? = null
                try {
                    // Only between two readings of a connected listener: once it binds (the process
                    // started in quiet mode) or comes back, what it finds already posted is the new
                    // baseline, not a burst of notifications that came in.
                    counts.read.collect { now ->
                        val previous = before
                        if (previous != null && now != null) gathered.addGrowth(previous, now)
                        before = now
                    }
                } finally {
                    // Cancelled by quiet mode ending: the only way this collection stops.
                    mutableAway.value = gathered.toMap()
                }
            }
        }
    }

    /** Home showed the digest: it goes until the next quiet session. */
    fun clear() {
        mutableAway.value = emptyMap()
    }

    private fun HashMap<PackageUser, Int>.addGrowth(previous: Map<PackageUser, Int>, now: Map<PackageUser, Int>) {
        now.forEach { (owner, count) ->
            val grown = count - (previous[owner] ?: 0)
            if (grown > 0) this[owner] = (this[owner] ?: 0) + grown
        }
    }
}
