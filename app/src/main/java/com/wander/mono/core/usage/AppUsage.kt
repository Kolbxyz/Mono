package com.wander.mono.core.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.apps.LaunchableApp
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Today's time in an app, read once when its long-press options open (never on a timer), from the
 * usage access the user grants in Android's settings. Only Mono's own profile is readable.
 */
@Singleton
class AppUsage @Inject constructor(@ApplicationContext private val context: Context) {
    private val stats = context.getSystemService(UsageStatsManager::class.java)
    private val appOps = context.getSystemService(AppOpsManager::class.java)

    fun isGranted(): Boolean {
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openSettings(): ActionResult = try {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        ActionResult.Done
    } catch (_: ActivityNotFoundException) {
        ActionResult.Failed(R.string.error_settings_missing)
    }

    /** Milliseconds [app] was in front since midnight; null without access or for another profile. */
    suspend fun todayMillis(app: LaunchableApp): Long? {
        if (!app.isMainProfile || !isGranted()) return null
        val packageName = app.key.packageName
        return todayByPackage { it == packageName }[packageName] ?: 0L
    }

    /** Milliseconds each app of Mono's own profile was in front since midnight; null without access. */
    suspend fun todayMillisByPackage(): Map<String, Long>? = if (isGranted()) todayByPackage { true } else null

    private suspend fun todayByPackage(wanted: (String) -> Boolean): Map<String, Long> = withContext(Dispatchers.IO) {
        val end = System.currentTimeMillis()
        val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        marks(start, end, wanted).mapValues { (_, marks) -> foregroundMillis(marks, start, end) }
    }

    /** The activity marks of each [wanted] package between [start] and [end], in time order. */
    private fun marks(start: Long, end: Long, wanted: (String) -> Boolean): Map<String, List<ActivityMark>> {
        val events = stats.queryEvents(start, end)
        val event = UsageEvents.Event()
        val marks = mutableMapOf<String, MutableList<ActivityMark>>()
        while (events.getNextEvent(event)) {
            val resumed = when (event.eventType) {
                RESUMED -> true
                PAUSED -> false
                else -> null
            }
            if (resumed != null && wanted(event.packageName)) {
                val mark = ActivityMark(event.className.orEmpty(), resumed, event.timeStamp)
                marks.getOrPut(event.packageName) { mutableListOf() } += mark
            }
        }
        return marks
    }

    private companion object {
        // ACTIVITY_RESUMED / ACTIVITY_PAUSED since Android 10; the same values as the older
        // MOVE_TO_FOREGROUND / MOVE_TO_BACKGROUND, so they work on every supported version.
        const val RESUMED = 1
        const val PAUSED = 2
    }
}
