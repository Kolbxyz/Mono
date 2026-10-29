package com.wander.mono.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/** Whether the drawer is open (kept across rotation and process death) and what a home press does. */
@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    messages: UserMessages,
) : ViewModel() {
    val drawerOpen: StateFlow<Boolean> = savedState.getStateFlow(DRAWER_OPEN, false)
    val messages: SharedFlow<Int> = messages.messages

    private val mutableDrawerAnimates = MutableStateFlow(true)

    /** False when the drawer was closed while Mono was not on screen: there is nothing to animate. */
    val drawerAnimates: StateFlow<Boolean> = mutableDrawerAnimates.asStateFlow()

    private val mutableScrollToTop = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToTop: SharedFlow<Unit> = mutableScrollToTop.asSharedFlow()

    fun openDrawer() {
        mutableDrawerAnimates.value = true
        savedState[DRAWER_OPEN] = true
    }

    fun closeDrawer() {
        mutableDrawerAnimates.value = true
        savedState[DRAWER_OPEN] = false
    }

    /**
     * Mono left the screen: whatever brings it back (home, back from an app, recents) lands on home,
     * not on a drawer left open. Closed without animation: Compose's frame clock is paused while the
     * activity is stopped, so an animated close would only start on return and play the drawer
     * fading out over home, the overlap seen right after launch.
     */
    fun leaveToHome() {
        mutableDrawerAnimates.value = false
        savedState[DRAWER_OPEN] = false
    }

    /**
     * Home pressed while Mono is already in front: the first press closes the drawer, the next
     * scrolls the home list back to the top. Coming back from another app only closes the drawer.
     */
    fun onHomePressed(alreadyOnHome: Boolean) {
        val wasOpen = drawerOpen.value
        closeDrawer()
        if (alreadyOnHome && !wasOpen) mutableScrollToTop.tryEmit(Unit)
    }

    private companion object {
        const val DRAWER_OPEN = "drawer_open"
    }
}
