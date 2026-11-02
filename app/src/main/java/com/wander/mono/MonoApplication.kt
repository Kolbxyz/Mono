package com.wander.mono

import android.app.Application
import com.wander.mono.core.quiet.QuietEnd
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MonoApplication : Application() {
    @Inject lateinit var quietEnd: QuietEnd

    override fun onCreate() {
        super.onCreate()
        quietEnd.start()
    }
}
