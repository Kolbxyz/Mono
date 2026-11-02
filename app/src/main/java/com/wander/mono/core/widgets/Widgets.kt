package com.wander.mono.core.widgets

import android.content.Context
import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.WidgetPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart

/** A placed widget whose provider is still installed, with the provider's label for TalkBack and the sheet. */
data class PlacedWidget(val placement: WidgetPlacement, val label: String, val canReconfigure: Boolean)

/** The widgets on home: their placements, kept in step with what the widget service still has bound. */
@Singleton
class Widgets @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: WidgetPrefs,
    private val host: WidgetHost,
) {
    /** Placements whose widget is gone are left out here and dropped for good by [prune]. */
    val placed: Flow<List<PlacedWidget>> = combine(
        prefs.placements,
        host.providersChanged.onStart { emit(Unit) },
    ) { placements, _ ->
        placements.mapNotNull { placement ->
            host.info(placement.appWidgetId)?.let { info ->
                PlacedWidget(placement, info.loadLabel(context.packageManager), info.isReconfigurable())
            }
        }
    }
        // A binder read and a label load per widget: never on the main thread.
        .flowOn(Dispatchers.IO)

    suspend fun add(placement: WidgetPlacement) = prefs.edit { it + placement }

    suspend fun remove(appWidgetId: Int) {
        prefs.edit { list -> list.filterNot { it.appWidgetId == appWidgetId } }
        host.delete(appWidgetId)
    }

    suspend fun resize(appWidgetId: Int, size: WidgetSize) = prefs.edit { list ->
        list.map { if (it.appWidgetId == appWidgetId) it.copy(heightDp = size.heightDp) else it }
    }

    /** [favorites] are the ones on home right now, top to bottom. */
    suspend fun move(appWidgetId: Int, by: Int, favorites: List<AppKey>) =
        prefs.edit { moveWidget(favorites, it, appWidgetId, by) }

    /**
     * Drops placements whose widget died (provider uninstalled) and frees ids the host holds that
     * no placement uses (a setup interrupted by the process dying), except the ones in [keep].
     */
    suspend fun prune(keep: Set<Int>) {
        val dead = mutableListOf<Int>()
        prefs.edit { list ->
            list.filter { placement ->
                (host.info(placement.appWidgetId) != null).also { alive -> if (!alive) dead += placement.appWidgetId }
            }
        }
        val placedIds = prefs.current().map { it.appWidgetId }.toSet()
        (dead + (host.allIds() - placedIds - keep)).distinct().forEach(host::delete)
    }
}
