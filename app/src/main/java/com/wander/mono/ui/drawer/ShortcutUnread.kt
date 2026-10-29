package com.wander.mono.ui.drawer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.wander.mono.core.notifications.ConversationKey
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.core.siblings.PhalanxUnread
import com.wander.mono.R
import com.wander.mono.ui.components.AppBadge
import com.wander.mono.ui.components.CountBadge
import com.wander.mono.ui.components.NotificationDot
import com.wander.mono.ui.components.appBadge

/**
 * Marks conversation shortcuts (a Phalanx room, a Signal chat) with the unread messages of the
 * notifications posted for them, matched by the shortcut id the app puts on its notifications.
 */
internal fun List<SearchHit>.withUnread(unread: Map<ConversationKey, Int>, showCounts: Boolean): List<SearchHit> {
    if (unread.isEmpty()) return this
    return map { hit ->
        if (hit !is SearchHit.Shortcut) return@map hit
        val sc = hit.shortcut
        val count = unread[ConversationKey(PackageUser(sc.packageName, sc.userSerial), sc.id)] ?: 0
        hit.copy(badge = appBadge(count, showCounts))
    }
}

/**
 * Phalanx's own per-room counts on its conversation shortcuts (the shortcut id is the room id).
 * They replace what notifications said, since they include muted rooms and rooms read elsewhere
 * drop out. A room marked unread without a count gets a dot.
 */
internal fun List<SearchHit>.withRoomUnread(phalanx: PhalanxUnread?, showCounts: Boolean): List<SearchHit> {
    if (phalanx == null) return this
    return map { hit ->
        val sc = (hit as? SearchHit.Shortcut)?.shortcut
        if (sc == null || sc.packageName != phalanx.packageName || sc.userSerial != phalanx.userSerial) {
            return@map hit
        }
        val room = phalanx.rooms[sc.id]
        val badge = when {
            room == null -> AppBadge.None
            room.unread > 0 -> appBadge(room.unread, showCounts)
            else -> AppBadge.Dot
        }
        hit.copy(badge = badge)
    }
}

/** The shortcut's unread marker, after its label. */
@Composable
fun ShortcutBadge(badge: AppBadge) {
    when (badge) {
        AppBadge.None -> Unit
        AppBadge.Dot -> NotificationDot()
        is AppBadge.Count -> CountBadge(badge)
    }
}

/** Announces the unread count on the whole row, since the pill itself is not read out. */
@Composable
fun Modifier.unreadState(badge: AppBadge): Modifier {
    val count = (badge as? AppBadge.Count)?.count ?: return this
    val state = pluralStringResource(R.plurals.state_unread_messages, count, count)
    return semantics { stateDescription = state }
}
