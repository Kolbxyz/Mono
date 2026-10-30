package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wander.mono.core.apps.AppKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.folderStore by preferencesDataStore(name = "folders")

/**
 * The user's folders on home. A file of its own, so grouping never touches the saved home order:
 * a folder sits where its first favourite is. Keys of apps removed from home are kept, like
 * favourites: adding the app back puts it back in its folder.
 */
@Singleton
class HomeFolders @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.folderStore

    val folders: Flow<List<HomeFolder>> = store.data
        .map { it.readFolders() }
        .distinctUntilChanged()

    /** A new folder named [name] holding [app], taken out of any other folder; a blank name does nothing. */
    suspend fun create(name: String, app: AppKey) {
        val clean = cleanFolderName(name)
        if (clean.isEmpty()) return
        val folder = HomeFolder(UUID.randomUUID().toString(), clean, emptyList())
        edit { (it + folder).withApp(folder.id, app) }
    }

    suspend fun add(id: String, app: AppKey) = edit { it.withApp(id, app) }

    suspend fun remove(app: AppKey) = edit { it.withoutApp(app) }

    /** A blank [name] keeps the current one. */
    suspend fun rename(id: String, name: String) {
        val clean = cleanFolderName(name)
        if (clean.isNotEmpty()) edit { folders -> folders.map { if (it.id == id) it.copy(name = clean) else it } }
    }

    /** Ungroups the folder: its apps stay on home, where they are. */
    suspend fun dissolve(id: String) = edit { folders -> folders.filterNot { it.id == id } }

    /** Replaces every folder, for a restored backup. */
    suspend fun replace(folders: List<HomeFolder>) {
        store.edit { it.writeFolders(folders) }
    }

    private suspend fun edit(change: (List<HomeFolder>) -> List<HomeFolder>) {
        store.edit { prefs -> prefs.writeFolders(change(prefs.readFolders())) }
    }

    private fun Preferences.readFolders(): List<HomeFolder> =
        this[FOLDERS].orEmpty().lines().mapNotNull(HomeFolder::decode)

    private fun MutablePreferences.writeFolders(folders: List<HomeFolder>) {
        this[FOLDERS] = folders.joinToString("\n") { it.encode() }
    }

    private companion object {
        val FOLDERS = stringPreferencesKey("folders")
    }
}
