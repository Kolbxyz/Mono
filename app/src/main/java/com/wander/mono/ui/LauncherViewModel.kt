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

/**
 * What is open over home (drawer, settings, home options; kept across rotation and process death)
 * and what a home press does. Settings opens over the drawer, so back from settings returns there.
 */
@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    messages: UserMessages,
) : ViewModel() {
    val drawerOpen: StateFlow<Boolean> = savedState.getStateFlow(DRAWER_OPEN, false)
    val settingsOpen: StateFlow<Boolean> = savedState.getStateFlow(SETTINGS_OPEN, false)
    val homeOptionsOpen: StateFlow<Boolean> = savedState.getStateFlow(HOME_OPTIONS_OPEN, false)

    /** Edit mode: favourites can be dragged, renamed, removed and their live rows switched. */
    val editing: StateFlow<Boolean> = savedState.getStateFlow(EDITING, false)

    /** The drawer was opened from edit mode to pick an app to add to home. */
    val pickingApp: StateFlow<Boolean> = savedState.getStateFlow(PICKING_APP, false)
    val messages: SharedFlow<Int> = messages.messages

    private val mutableOverlaysAnimate = MutableStateFlow(true)

    /** False when drawer and settings were closed while Mono was not on screen: nothing to animate. */
    val overlaysAnimate: StateFlow<Boolean> = mutableOverlaysAnimate.asStateFlow()

    private val mutableScrollToTop = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToTop: SharedFlow<Unit> = mutableScrollToTop.asSharedFlow()

    fun openDrawer() = setAnimated(DRAWER_OPEN, true)

    fun closeDrawer() {
        savedState[PICKING_APP] = false
        setAnimated(DRAWER_OPEN, false)
    }

    fun setEditing(editing: Boolean) {
        savedState[HOME_OPTIONS_OPEN] = false
        savedState[EDITING] = editing
    }

    /** Opens the drawer so the next app tapped is added to home instead of launched. */
    fun pickApp() {
        savedState[PICKING_APP] = true
        openDrawer()
    }

    fun openSettings() {
        savedState[HOME_OPTIONS_OPEN] = false
        setAnimated(SETTINGS_OPEN, true)
    }

    fun closeSettings() = setAnimated(SETTINGS_OPEN, false)

    /** The long-press options for home itself, opened from the clock or an empty spot. */
    fun showHomeOptions(show: Boolean) {
        savedState[HOME_OPTIONS_OPEN] = show
    }

    /**
     * Mono left the screen: whatever brings it back (home, back from an app, recents) lands on home,
     * not on a drawer or settings left open. Closed without animation: Compose's frame clock is
     * paused while the activity is stopped, so an animated close would only start on return and
     * play the overlay fading out over home, the overlap seen right after launch.
     */
    fun leaveToHome() {
        mutableOverlaysAnimate.value = false
        savedState[DRAWER_OPEN] = false
        savedState[SETTINGS_OPEN] = false
        savedState[PICKING_APP] = false
        savedState[EDITING] = false
    }

    /**
     * Home pressed while Mono is already in front: the first press closes what is open, the next
     * scrolls the home list back to the top. Coming back from another app only closes.
     */
    fun onHomePressed(alreadyOnHome: Boolean) {
        val wasOpen = drawerOpen.value || settingsOpen.value || editing.value
        closeDrawer()
        closeSettings()
        showHomeOptions(false)
        setEditing(false)
        if (alreadyOnHome && !wasOpen) mutableScrollToTop.tryEmit(Unit)
    }

    private fun setAnimated(key: String, open: Boolean) {
        mutableOverlaysAnimate.value = true
        savedState[key] = open
    }

    private companion object {
        const val DRAWER_OPEN = "drawer_open"
        const val SETTINGS_OPEN = "settings_open"
        const val HOME_OPTIONS_OPEN = "home_options_open"
        const val EDITING = "editing"
        const val PICKING_APP = "picking_app"
    }
}
