package com.wander.mono.core.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Whether Mono may read the calendar. Re-read after the permission prompt answers and when the
 * home screen starts (the user may have granted it in App info), never on a timer. Revoking it
 * kills the process, so a stale true never outlives the grant.
 */
@Singleton
class CalendarAccess @Inject constructor(@ApplicationContext private val context: Context) {
    private val mutableGranted = MutableStateFlow(isGranted())
    val granted: StateFlow<Boolean> = mutableGranted.asStateFlow()

    fun recheck() {
        mutableGranted.value = isGranted()
    }

    private fun isGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val PERMISSION = Manifest.permission.READ_CALENDAR
    }
}
