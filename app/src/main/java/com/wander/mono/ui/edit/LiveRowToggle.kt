package com.wander.mono.ui.edit

import com.wander.mono.data.LiveRowRef

/** A live row switch under a favourite in edit mode. */
data class LiveRowToggle(val row: LiveRowRef, val shown: Boolean)
