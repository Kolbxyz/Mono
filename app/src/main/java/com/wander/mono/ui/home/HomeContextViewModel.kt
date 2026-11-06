package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import com.wander.mono.core.notifications.QuietDigest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Home reading the moment, all on the device: the quiet digest, cleared once seen. */
@HiltViewModel
class HomeContextViewModel @Inject constructor(private val digest: QuietDigest) : ViewModel() {
    fun digestSeen() = digest.clear()
}
