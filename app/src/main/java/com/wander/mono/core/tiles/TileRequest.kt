package com.wander.mono.core.tiles

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import com.wander.mono.MainActivity

/**
 * What a Quick Settings tile asks of home, carried as an extra on the intent that brings Mono to
 * the front. The private space is locked and unlocked from there: Android lets only the default
 * home app in front do that.
 */
enum class TileRequest(private val stored: String) {
    Search("search"),
    LockPrivateSpace("lock_private_space"),
    UnlockPrivateSpace("unlock_private_space"),
    ;

    companion object {
        private const val EXTRA = "com.wander.mono.extra.TILE_REQUEST"

        /** The request [intent] carries, or null for any other intent. */
        fun of(intent: Intent?): TileRequest? =
            intent?.getStringExtra(EXTRA)?.let { stored -> entries.firstOrNull { it.stored == stored } }

        /**
         * Collapses the shade and opens Mono with [request]; on the lock screen, after the user
         * unlocks the phone, which the system asks for.
         */
        fun TileService.openMono(request: TileRequest) {
            if (isLocked) unlockAndRun { start(request) } else start(request)
        }

        private fun TileService.start(request: TileRequest) {
            val intent = Intent(this, MainActivity::class.java)
                .putExtra(EXTRA, request.stored)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(pendingIntent(this, request, intent))
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
        }

        private fun pendingIntent(context: Context, request: TileRequest, intent: Intent): PendingIntent =
            PendingIntent.getActivity(
                context,
                request.ordinal,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
    }
}
