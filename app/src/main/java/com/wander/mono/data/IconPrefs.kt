package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wander.mono.core.apps.IconMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.iconStore by preferencesDataStore(name = "icons")

/** How app rows show icons, stored under `icon_mode` as [IconMode.stored]. */
@Singleton
class IconPrefs @Inject constructor(@ApplicationContext context: Context) {
    val mode: Flow<IconMode> = context.iconStore.data
        .map { IconMode.fromStored(it[MODE]) }
        .distinctUntilChanged()

    private companion object {
        val MODE = stringPreferencesKey("icon_mode")
    }
}
