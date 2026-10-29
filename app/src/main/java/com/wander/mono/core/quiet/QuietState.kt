package com.wander.mono.core.quiet

import com.wander.mono.core.apps.AppKey

/** Quiet mode on or off, and the favourites it keeps on home. */
data class QuietState(val enabled: Boolean, val apps: Set<AppKey>)
