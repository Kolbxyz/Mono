package com.wander.mono

import android.app.Application
import com.wander.mono.core.quiet.QuietEnd
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class MonoApplication : Application() {
    @Inject lateinit var quietEnd: QuietEnd

    override fun onCreate() {
        super.onCreate()
        quietEnd.start()
        forgetLaunchHours()
    }

    /**
     * Earlier versions kept how often each app was opened at each hour (for a likely-favourite pill,
     * since removed). Nothing reads it now, so it leaves the phone too: a habit is not kept for nothing.
     */
    private fun forgetLaunchHours() {
        File(filesDir, "datastore/launch_hours.preferences_pb").delete()
    }
}
