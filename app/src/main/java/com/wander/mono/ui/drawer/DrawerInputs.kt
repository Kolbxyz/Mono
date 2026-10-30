package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.WorkProfile
import com.wander.mono.core.contacts.Contact
import com.wander.mono.core.notifications.ConversationKey
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.NotificationPreviews
import com.wander.mono.core.siblings.PhalanxRooms
import com.wander.mono.core.siblings.PhalanxUnread
import com.wander.mono.core.siblings.WandaTrack
import com.wander.mono.core.system.SettingsPage
import com.wander.mono.data.RecentSearch
import com.wander.mono.data.settings.BadgeSettings
import com.wander.mono.data.settings.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** What search looks in besides the apps themselves, for [query]; sources switched off come in empty. */
internal fun searchableFor(sources: SearchSources, memory: SearchMemory, query: Flow<String>): Flow<Searchable> =
    combine(
        sources.shortcuts(),
        sources.contacts(query),
        sources.tracks(query),
        sources.pages(),
        combine(sources.quickAnswers(), sources.notices(), memory.recent, ::SearchExtras),
        ::Searchable,
    )

/** Unread per conversation, from notifications and from Phalanx, for the shortcut results' badges. */
internal fun conversationUnread(
    previews: NotificationPreviews,
    phalanx: PhalanxRooms,
    settings: SettingsStore,
): Flow<ConversationUnread> =
    combine(previews.conversations, phalanx.unread, settings[BadgeSettings.ShowCounts], ::ConversationUnread)

internal data class Searchable(
    val shortcuts: List<AppShortcut>,
    val contacts: QueryResults<Contact>,
    val tracks: QueryResults<WandaTrack>,
    val pages: List<SettingsPage>,
    val more: SearchExtras,
)

/** Whether quick answers (and hand-offs) are on, the notifications to look in, the remembered searches. */
internal data class SearchExtras(
    val quickAnswers: Boolean,
    val notices: List<NotificationPreview>,
    val recent: List<RecentSearch>,
)

/** Every app, and the work profiles to list theirs apart. */
internal data class Profiled(val all: List<DrawerEntry>, val work: List<WorkProfile>)

/** Unread per conversation from notifications, and from Phalanx itself when it publishes it. */
internal data class ConversationUnread(
    val notified: Map<ConversationKey, Int>,
    val phalanx: PhalanxUnread?,
    val showCounts: Boolean,
)
