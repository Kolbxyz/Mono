package com.wander.mono.data.settings

object SearchSettings {
    /** Find contacts by name in search. Off by default: it needs `READ_CONTACTS`, asked for when switched on. */
    val Contacts = BooleanSetting("search_contacts", default = false)
}
