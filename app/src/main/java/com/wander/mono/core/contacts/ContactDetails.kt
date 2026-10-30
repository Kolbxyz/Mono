package com.wander.mono.core.contacts

import android.content.ContentResolver
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Data

/** A contact's number and address to reach them by; either is null when the contact has none. */
internal data class ContactDetails(val phone: String?, val email: String?)

/**
 * The phone number and email address of each contact in [ids], in one query of the contacts
 * provider's data rows (already covered by `READ_CONTACTS`). The one marked default wins, else the
 * first listed. Contacts with neither are left out.
 */
internal fun ContentResolver.detailsOf(ids: List<Long>): Map<Long, ContactDetails> {
    if (ids.isEmpty()) return emptyMap()
    val selection = "${Data.CONTACT_ID} IN (${ids.joinToString(",") { "?" }}) AND ${Data.MIMETYPE} IN (?, ?)"
    val args = ids.map { it.toString() } + Phone.CONTENT_ITEM_TYPE + Email.CONTENT_ITEM_TYPE
    val projection = arrayOf(Data.CONTACT_ID, Data.MIMETYPE, Data.DATA1)
    val phones = HashMap<Long, String>()
    val emails = HashMap<Long, String>()
    val defaultFirst = "${Data.IS_SUPER_PRIMARY} DESC"
    query(Data.CONTENT_URI, projection, selection, args.toTypedArray(), defaultFirst)?.use { cursor ->
        while (cursor.moveToNext()) {
            val value = cursor.getString(VALUE)?.takeIf { it.isNotBlank() } ?: continue
            val into = if (cursor.getString(MIMETYPE) == Phone.CONTENT_ITEM_TYPE) phones else emails
            into.putIfAbsent(cursor.getLong(CONTACT_ID), value)
        }
    }
    return (phones.keys + emails.keys).associateWith { ContactDetails(phones[it], emails[it]) }
}

// Column positions in the projection above.
private const val CONTACT_ID = 0
private const val MIMETYPE = 1
private const val VALUE = 2
