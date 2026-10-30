package com.wander.mono.ui.settings

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.wander.mono.R

/** Each access Mono may hold, in plain words: what it is, what Mono does with it. */
enum class PrivacyAccess(@StringRes val title: Int, @StringRes val use: Int, @DrawableRes val icon: Int) {
    Notifications(R.string.privacy_notifications, R.string.privacy_notifications_use, R.drawable.sym_notifications),
    Usage(R.string.privacy_usage, R.string.privacy_usage_use, R.drawable.sym_hourglass_bottom),
    Calendar(R.string.privacy_calendar, R.string.privacy_calendar_use, R.drawable.sym_event),
    Contacts(R.string.privacy_contacts, R.string.privacy_contacts_use, R.drawable.sym_person),
    DoNotDisturb(R.string.privacy_dnd, R.string.privacy_dnd_use, R.drawable.sym_do_not_disturb_on),
    Gestures(R.string.privacy_gestures, R.string.privacy_gestures_use, R.drawable.sym_swipe),
    HiddenProfiles(R.string.privacy_hidden_profiles, R.string.privacy_hidden_profiles_use, R.drawable.sym_lock),
}

/**
 * The accesses that exist on Android [sdkInt]: switching Mono's own Do Not Disturb mode needs
 * Android 10, and the private space (hidden profiles) arrived in Android 15.
 */
fun accessesOn(sdkInt: Int): List<PrivacyAccess> = PrivacyAccess.entries.filter { access ->
    when (access) {
        PrivacyAccess.DoNotDisturb -> sdkInt >= Build.VERSION_CODES.Q
        PrivacyAccess.HiddenProfiles -> sdkInt >= Build.VERSION_CODES.VANILLA_ICE_CREAM
        else -> true
    }
}

/** One access and whether Mono holds it right now. */
data class AccessState(val access: PrivacyAccess, val granted: Boolean)
