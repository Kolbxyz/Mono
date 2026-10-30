package com.wander.mono.core.calendar

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.provider.CalendarContract.Attendees
import android.provider.CalendarContract.Instances
import com.wander.mono.core.system.SystemTime
import com.wander.mono.data.settings.CalendarSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.di.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

/**
 * Upcoming calendar events, while the setting is on and the permission granted; null otherwise.
 * Read again when the provider reports a change (a `ContentObserver`) and, on the clock tick, only
 * once the next boundary is reached (an event starts or ends, a day passes); never every minute.
 */
@Singleton
class CalendarEvents @Inject constructor(
    @ApplicationContext private val context: Context,
    access: CalendarAccess,
    settings: SettingsStore,
    private val time: SystemTime,
    @AppScope scope: CoroutineScope,
) {
    private val resolver = context.contentResolver

    val agenda: StateFlow<CalendarAgenda?> =
        combine(settings[CalendarSettings.Enabled], access.granted) { on, granted -> on && granted }
            .distinctUntilChanged()
            .flatMapLatest { active -> if (active) changes().flatMapLatest { reads() } else flowOf(null) }
            .stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** One signal now, then one per provider change (sync, edit, calendar shown or hidden). */
    private fun changes(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(CalendarContract.CONTENT_URI, true, observer)
        trySend(Unit)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }.conflate()

    /** Reads now, then again on the first clock tick at or past the boundary the read computed. */
    private fun reads(): Flow<CalendarAgenda> = flow {
        var boundary = Long.MIN_VALUE
        time.now.collect { now ->
            if (now < boundary) return@collect
            val zone = ZoneId.systemDefault()
            val events = query(now, zone)
            boundary = nextBoundary(events, now, zone)
            emit(CalendarAgenda(upcomingEvents(events, now), now, calendarApp(), canOpenMaps()))
        }
    }.flowOn(Dispatchers.IO)

    private fun query(now: Long, zone: ZoneId): List<CalendarEvent> {
        val uri = Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, now - QUERY_BEFORE_MS)
            ContentUris.appendId(it, now + QUERY_AFTER_MS)
        }.build()
        return resolver.query(uri, PROJECTION, "${Instances.VISIBLE} = 1", null, "${Instances.BEGIN} ASC")
            ?.use { cursor -> generateSequence { if (cursor.moveToNext()) cursor.toEvent(zone) else null }.toList() }
            .orEmpty()
    }

    private fun Cursor.toEvent(zone: ZoneId): CalendarEvent {
        val allDay = getInt(COL_ALL_DAY) != 0
        val begin = getLong(COL_BEGIN)
        val end = getLong(COL_END)
        val location = getString(COL_LOCATION)?.trim()?.ifEmpty { null }
        return CalendarEvent(
            eventId = getLong(COL_EVENT_ID),
            title = getString(COL_TITLE).orEmpty(),
            begin = if (allDay) allDayToLocal(begin, zone) else begin,
            end = if (allDay) allDayToLocal(end, zone) else end,
            allDay = allDay,
            declined = getInt(COL_STATUS) == Attendees.ATTENDEE_STATUS_DECLINED,
            location = location,
            meetingUrl = meetingLinkIn(location) ?: meetingLinkIn(getString(COL_DESCRIPTION)),
        )
    }

    /** Whether any app shows a place, resolved with the read so the card never offers a dead Map. */
    private fun canOpenMaps(): Boolean =
        context.packageManager.resolveActivity(mapIntent(""), PackageManager.MATCH_DEFAULT_ONLY) != null

    /** The default calendar app; with no default set the resolver is the system chooser, so null. */
    private fun calendarApp(): String? {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)
        val resolved = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolved?.activityInfo?.packageName?.takeIf { it != SYSTEM_PACKAGE }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val SYSTEM_PACKAGE = "android"

        val PROJECTION = arrayOf(
            Instances.EVENT_ID,
            Instances.TITLE,
            Instances.BEGIN,
            Instances.END,
            Instances.ALL_DAY,
            Instances.SELF_ATTENDEE_STATUS,
            Instances.EVENT_LOCATION,
            Instances.DESCRIPTION,
        )
        const val COL_EVENT_ID = 0
        const val COL_TITLE = 1
        const val COL_BEGIN = 2
        const val COL_END = 3
        const val COL_ALL_DAY = 4
        const val COL_STATUS = 5
        const val COL_LOCATION = 6
        const val COL_DESCRIPTION = 7
    }
}
