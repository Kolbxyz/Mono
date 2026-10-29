package com.wander.mono.core.system

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Copies text for the user, such as a calculator result. */
@Singleton
class Clipboard @Inject constructor(@ApplicationContext private val context: Context) {
    private val manager = context.getSystemService(ClipboardManager::class.java)

    /**
     * Copies [text], described to the system as [label]. True when the caller should confirm the
     * copy itself: from Android 13 the system shows its own confirmation, and a second would repeat it.
     */
    fun copy(@StringRes label: Int, text: String): Boolean {
        manager.setPrimaryClip(ClipData.newPlainText(context.getString(label), text))
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    }
}
