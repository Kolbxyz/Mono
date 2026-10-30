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
 * What is open over home (drawer, settings, home options, the clock and media sheets; kept across
 * rotation and process death) and what a home press does. Settings opens over the drawer, so back
 * from settings returns there.
 */
@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    messages: UserMessages,
) : ViewModel() {
    val drawerOpen: StateFlow<Boolean> = savedState.getStateFlow(DRAWER_OPEN, false)

    /** The drawer was opened from the search dock, so its search field takes focus and the keyboard. */
    val focusSearch: StateFlow<Boolean> = savedState.getStateFlow(FOCUS_SEARCH, false)
    val settingsOpen: StateFlow<Boolean> = savedState.getStateFlow(SETTINGS_OPEN, false)
    val homeOptionsOpen: StateFlow<Boolean> = savedState.getStateFlow(HOME_OPTIONS_OPEN, false)

    /** The clock's own sheet, from a long press on it: its face, live. */
    val clockSheetOpen: StateFlow<Boolean> = savedState.getStateFlow(CLOCK_SHEET_OPEN, false)

    /** The player's sheet, from a long press on it under the clock. */
    val mediaSheetOpen: StateFlow<Boolean> = savedState.getStateFlow(MEDIA_SHEET_OPEN, false)

    /** Edit mode: favourites can be dragged, renamed, removed and their live rows switched. */
    val editing: StateFlow<Boolean> = savedState.getStateFlow(EDITING, false)

    /** The drawer was opened from edit mode to pick an app to add to home. */
    val pickingApp: StateFlow<Boolean> = savedState.getStateFlow(PICKING_APP, false)
    val messages: SharedFlow<UserMessage> = messages.messages

    private val mutableOverlaysAnimate = MutableStateFlow(true)

    /** False when drawer and settings were closed while Mono was not on screen: nothing to animate. */
    val overlaysAnimate: StateFlow<Boolean> = mutableOverlaysAnimate.asStateFlow()

    private val mutableScrollToTop = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToTop: SharedFlow<Unit> = mutableScrollToTop.asSharedFlow()

    fun openDrawer(focusSearch: Boolean = false, picking: Boolean = false) {
        savedState[FOCUS_SEARCH] = focusSearch
        savedState[PICKING_APP] = picking
        setAnimated(DRAWER_OPEN, true)
    }

    /** Closes the drawer; what it was opened for stays until [drawerClosed], so it fades out unchanged. */
    fun closeDrawer() = setAnimated(DRAWER_OPEN, false)

    /** The drawer has finished closing and is off screen: the next opening starts afresh. */
    fun drawerClosed() {
        savedState[PICKING_APP] = false
        savedState[FOCUS_SEARCH] = false
    }

    fun setEditing(editing: Boolean) {
        savedState[HOME_OPTIONS_OPEN] = false
        savedState[EDITING] = editing
    }

    /** Opens the drawer so the next app tapped is added to home instead of launched. */
    fun pickApp() = openDrawer(picking = true)

    fun openSettings() {
        savedState[HOME_OPTIONS_OPEN] = false
        setAnimated(SETTINGS_OPEN, true)
    }

    fun closeSettings() = setAnimated(SETTINGS_OPEN, false)

    /** The long-press options for home itself, opened from the clock or an empty spot. */
    fun showHomeOptions(show: Boolean) {
        savedState[HOME_OPTIONS_OPEN] = show
    }

    fun showClockSheet(show: Boolean) {
        savedState[CLOCK_SHEET_OPEN] = show
    }

    fun showMediaSheet(show: Boolean) {
        savedState[MEDIA_SHEET_OPEN] = show
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
        savedState[FOCUS_SEARCH] = false
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
        showClockSheet(false)
        showMediaSheet(false)
        setEditing(false)
        if (alreadyOnHome && !wasOpen) mutableScrollToTop.tryEmit(Unit)
    }

    private fun setAnimated(key: String, open: Boolean) {
        mutableOverlaysAnimate.value = true
        savedState[key] = open
    }

    private companion object {
        const val DRAWER_OPEN = "drawer_open"
        const val FOCUS_SEARCH = "focus_search"
        const val SETTINGS_OPEN = "settings_open"
        const val HOME_OPTIONS_OPEN = "home_options_open"
        const val CLOCK_SHEET_OPEN = "clock_sheet_open"
        const val MEDIA_SHEET_OPEN = "media_sheet_open"
        const val EDITING = "editing"
        const val PICKING_APP = "picking_app"
    }
}
