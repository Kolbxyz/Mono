package com.wander.mono.core.contacts

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import androidx.core.content.ContextCompat
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** A contact found by name: just enough to show it and open it. */
data class Contact(val id: Long, val lookupKey: String, val name: String)

/**
 * Looks contacts up by name while the user types. Opt-in (`READ_CONTACTS`), queried fresh for each
 * query and never stored: nothing outlives the search that asked for it.
 */
@Singleton
class ContactSearch @Inject constructor(@ApplicationContext private val context: Context) {
    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
        PackageManager.PERMISSION_GRANTED

    /**
     * Contacts the provider matches for [query], at most [limit]; empty without the permission, so
     * revoking it in Android's settings simply hides contacts from search.
     */
    suspend fun find(query: String, limit: Int): List<Contact> = withContext(Dispatchers.IO) {
        if (!hasPermission() || query.isBlank()) return@withContext emptyList()
        val uri = Contacts.CONTENT_FILTER_URI.buildUpon()
            .appendPath(query.trim())
            .appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY, limit.toString())
            .build()
        val projection = arrayOf(Contacts._ID, Contacts.LOOKUP_KEY, Contacts.DISPLAY_NAME_PRIMARY)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val name = cursor.getString(NAME) ?: continue
                    add(Contact(cursor.getLong(ID), cursor.getString(LOOKUP_KEY), name))
                }
            }
        }.orEmpty()
    }

    fun open(contact: Contact): ActionResult = try {
        val uri = Contacts.getLookupUri(contact.id, contact.lookupKey)
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        ActionResult.Done
    } catch (_: ActivityNotFoundException) {
        ActionResult.Failed(R.string.error_no_contacts_app)
    }

    private companion object {
        // Column positions in the projection above.
        const val ID = 0
        const val LOOKUP_KEY = 1
        const val NAME = 2
    }
}
