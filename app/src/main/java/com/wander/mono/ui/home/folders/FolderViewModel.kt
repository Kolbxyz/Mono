package com.wander.mono.ui.home.folders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.HomeFolder
import com.wander.mono.data.HomeFolders
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Folders on home: which are open, the options sheet of one, and the name dialog, for a new
 * folder (from an app's options) or a rename. What is open survives rotation.
 */
@HiltViewModel
class FolderViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val store: HomeFolders,
) : ViewModel() {
    // Eager: closing a folder around a launched app reads it without anything on screen collecting it.
    val folders: StateFlow<List<HomeFolder>> =
        store.folders.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val mutableOpen = MutableStateFlow(savedState.get<ArrayList<String>>(OPEN).orEmpty().toSet())

    /** The ids of the folders showing their apps. */
    val open: StateFlow<Set<String>> = mutableOpen.asStateFlow()

    /** The folder whose options are showing; null when none is, or it was just dissolved. */
    val options: StateFlow<HomeFolder?> = combine(
        savedState.getStateFlow<String?>(OPTIONS, null),
        folders,
    ) { id, all -> all.firstOrNull { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    private val mutableNaming = MutableStateFlow<FolderNaming?>(null)

    /** The name dialog's purpose, while it is open. */
    val naming: StateFlow<FolderNaming?> = mutableNaming.asStateFlow()

    fun toggle(id: String) = setOpen(if (id in mutableOpen.value) mutableOpen.value - id else mutableOpen.value + id)

    /** Closes the folder holding [app], once the app is launched from it, so home is tidy on return. */
    fun closeAround(app: AppKey) {
        val holding = folders.value.filter { app in it.apps }.mapTo(HashSet()) { it.id }
        if (holding.any { it in mutableOpen.value }) setOpen(mutableOpen.value - holding)
    }

    fun showOptions(id: String?) {
        savedState[OPTIONS] = id
    }

    /** Names a new folder that will hold [app]. */
    fun startNewFolder(app: AppKey) {
        mutableNaming.value = FolderNaming.New(app)
    }

    fun startRename(folder: HomeFolder) {
        mutableNaming.value = FolderNaming.Rename(folder)
    }

    fun dismissNaming() {
        mutableNaming.value = null
    }

    /** Saves the dialog's [name]; a blank one changes nothing. */
    fun saveName(naming: FolderNaming, name: String) {
        dismissNaming()
        viewModelScope.launch {
            when (naming) {
                is FolderNaming.New -> store.create(name, naming.app)
                is FolderNaming.Rename -> store.rename(naming.folder.id, name)
            }
        }
    }

    fun addTo(id: String, app: AppKey) {
        viewModelScope.launch { store.add(id, app) }
    }

    fun takeOut(app: AppKey) {
        viewModelScope.launch { store.remove(app) }
    }

    /** Its apps stay on home, where they are; nothing to undo. */
    fun dissolve(id: String) {
        setOpen(mutableOpen.value - id)
        viewModelScope.launch { store.dissolve(id) }
    }

    private fun setOpen(ids: Set<String>) {
        mutableOpen.value = ids
        savedState[OPEN] = ArrayList(ids)
    }

    private companion object {
        const val OPEN = "open_folders"
        const val OPTIONS = "folder_options"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/** What the folder name dialog is for. */
sealed interface FolderNaming {
    data class New(val app: AppKey) : FolderNaming

    data class Rename(val folder: HomeFolder) : FolderNaming
}
