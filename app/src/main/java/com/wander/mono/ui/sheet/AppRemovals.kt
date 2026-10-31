package com.wander.mono.ui.sheet

import com.wander.mono.R
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.di.AppScope
import com.wander.mono.ui.MessageAction
import com.wander.mono.ui.UserMessages
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Hiding an app and taking a favourite off home (from its sheet or edit mode), each followed by
 * "Removed X · Undo" (spec 7.8). Undo puts things back exactly: a hidden favourite never left its
 * place, a removed one goes back to the index it had, and its folder kept its key. In the app's
 * scope, as the snackbar outlives the sheet that started it.
 */
@Singleton
class AppRemovals @Inject constructor(
    private val prefs: LauncherPrefs,
    private val catalog: AppCatalog,
    private val messages: UserMessages,
    @AppScope private val scope: CoroutineScope,
) {
    fun hide(key: AppKey) {
        val label = labelOf(key)
        scope.launch {
            prefs.setHidden(key, hidden = true)
            val undo = MessageAction(R.string.action_undo) { scope.launch { prefs.setHidden(key, hidden = false) } }
            messages.show(R.string.app_hidden_named, undo, listOf(label))
        }
    }

    fun removeFromHome(key: AppKey) {
        val label = labelOf(key)
        scope.launch {
            val index = prefs.favorites.first().indexOf(key)
            if (index < 0) return@launch
            prefs.removeFavorite(key)
            val undo = MessageAction(R.string.action_undo) { scope.launch { prefs.insertFavorite(key, index) } }
            messages.show(R.string.favorite_removed, undo, listOf(label))
        }
    }

    /** The name the user sees; the package name only if the app is gone from the catalog already. */
    private fun labelOf(key: AppKey): String =
        catalog.apps.value?.firstOrNull { it.key == key }?.label ?: key.packageName
}
