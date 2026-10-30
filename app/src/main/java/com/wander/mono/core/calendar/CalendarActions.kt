package com.wander.mono.core.calendar

import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.annotation.StringRes
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Opens an event in the user's calendar app, at the occurrence that was tapped, and hands its place
 * to a maps app or its meeting link to whichever app takes it. Mono itself fetches nothing.
 */
@Singleton
class CalendarActions @Inject constructor(@ApplicationContext private val context: Context) {
    fun open(event: CalendarEvent): ActionResult {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.eventId))
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.begin)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.end)
            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, event.allDay)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            ActionResult.Done
        } catch (_: ActivityNotFoundException) {
            ActionResult.Failed(R.string.error_calendar_missing)
        }
    }

    /** The event's place in the maps app, searched by its text (no coordinates are known). */
    fun openMap(event: CalendarEvent): ActionResult {
        val location = event.location ?: return ActionResult.Failed(R.string.error_no_maps_app)
        return start(mapIntent(location), R.string.error_no_maps_app)
    }

    /** The event's meeting link, in the meeting app if one claims it, else the browser. */
    fun join(event: CalendarEvent): ActionResult {
        val url = event.meetingUrl ?: return ActionResult.Failed(R.string.error_meeting_link)
        return start(Intent(Intent.ACTION_VIEW, Uri.parse(url)), R.string.error_meeting_link)
    }

    private fun start(intent: Intent, @StringRes missing: Int): ActionResult = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        ActionResult.Done
    } catch (_: ActivityNotFoundException) {
        ActionResult.Failed(missing)
    }
}

/** A `geo:` search for [location], which every maps app answers. */
internal fun mapIntent(location: String): Intent =
    Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(location)))
