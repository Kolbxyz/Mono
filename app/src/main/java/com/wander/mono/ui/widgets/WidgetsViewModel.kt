package com.wander.mono.ui.widgets

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.Profiles
import com.wander.mono.core.widgets.HomeSlot
import com.wander.mono.core.widgets.MonoWidgetHostView
import com.wander.mono.core.widgets.PlacedWidget
import com.wander.mono.core.widgets.WidgetApp
import com.wander.mono.core.widgets.WidgetHost
import com.wander.mono.core.widgets.WidgetProviders
import com.wander.mono.core.widgets.WidgetSetup
import com.wander.mono.core.widgets.WidgetSize
import com.wander.mono.core.widgets.Widgets
import com.wander.mono.core.widgets.defaultAnchor
import com.wander.mono.core.widgets.homeSlots
import com.wander.mono.data.LauncherPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** What the options sheet of one widget row offers. */
data class WidgetOptionsState(val widget: PlacedWidget, val canMoveUp: Boolean, val canMoveDown: Boolean) {
    val size: WidgetSize get() = WidgetSize.nearest(widget.placement.heightDp)
}

/** The widget picker and the long-press options of widget rows; both survive rotation through saved state. */
@HiltViewModel
class WidgetsViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    catalog: AppCatalog,
    prefs: LauncherPrefs,
    private val providers: WidgetProviders,
    private val widgets: Widgets,
    private val setup: WidgetSetup,
    private val profiles: Profiles,
    private val host: WidgetHost,
) : ViewModel() {
    val pickerOpen: StateFlow<Boolean> = savedState.getStateFlow(PICKER_OPEN, false)
    private val optionsTarget: StateFlow<Int?> = savedState.getStateFlow(OPTIONS_TARGET, null)

    /** The favourites on home right now, top to bottom (uninstalled ones are not on screen). */
    private val homeFavorites: Flow<List<AppKey>> = combine(
        prefs.favorites,
        catalog.apps.filterNotNull(),
    ) { favs, apps ->
        val installed = apps.mapTo(HashSet()) { it.key }
        favs.distinct().filter { it in installed }
    }

    /** Null while loading; read afresh each time the picker opens, so new apps' widgets are there. */
    val pickerApps: StateFlow<List<WidgetApp>?> = pickerOpen.flatMapLatest<Boolean, List<WidgetApp>?> { open ->
        if (open) flow { emit(withContext(Dispatchers.Default) { providers.load() }) } else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** Null when no options sheet is open, or its widget has just gone. */
    val options: StateFlow<WidgetOptionsState?> = combine(
        optionsTarget,
        widgets.placed,
        homeFavorites,
    ) { target, placed, favorites ->
        val widget = placed.firstOrNull { it.placement.appWidgetId == target } ?: return@combine null
        val slots = homeSlots(favorites, placed.map { it.placement })
        val index = slots.indexOfFirst { it is HomeSlot.Widget && it.placement.appWidgetId == target }
        WidgetOptionsState(widget, canMoveUp = index > 0, canMoveDown = index < slots.lastIndex)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** A placed widget's view, made in the activity's [context]; the row composable owns it. */
    fun createView(context: Context, appWidgetId: Int): MonoWidgetHostView = host.createView(context, appWidgetId)

    /** A picker preview built from the provider's preview layout. */
    @RequiresApi(Build.VERSION_CODES.S)
    fun createPreview(context: Context, info: AppWidgetProviderInfo): View = host.createPreview(context, info)

    fun openPicker() {
        savedState[PICKER_OPEN] = true
    }

    fun closePicker() {
        savedState[PICKER_OPEN] = false
    }

    fun pick(info: AppWidgetProviderInfo) {
        closePicker()
        viewModelScope.launch {
            val serial = profiles.serialOf(info.profile)
            val anchor = defaultAnchor(homeFavorites.first(), info.provider.packageName, serial)
            setup.add(info, anchor)
        }
    }

    fun showOptions(appWidgetId: Int) {
        savedState[OPTIONS_TARGET] = appWidgetId
    }

    fun dismissOptions() {
        savedState[OPTIONS_TARGET] = null
    }

    /** Keeps the sheet open, so sizes can be compared in place. */
    fun resize(appWidgetId: Int, size: WidgetSize) {
        viewModelScope.launch { widgets.resize(appWidgetId, size) }
    }

    /** Keeps the sheet open, so the widget can be moved several rows in a row. */
    fun move(appWidgetId: Int, by: Int) {
        viewModelScope.launch { widgets.move(appWidgetId, by, homeFavorites.first()) }
    }

    fun reconfigure(appWidgetId: Int) {
        dismissOptions()
        setup.reconfigure(appWidgetId)
    }

    fun remove(appWidgetId: Int) {
        dismissOptions()
        viewModelScope.launch { widgets.remove(appWidgetId) }
    }

    private companion object {
        const val PICKER_OPEN = "widget_picker_open"
        const val OPTIONS_TARGET = "widget_options_target"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
