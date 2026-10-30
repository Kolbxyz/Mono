package com.wander.mono.core.contacts

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hands a contact to the app that calls, texts or mails it. Hand-off only: a call opens the dialer
 * with the number filled in (`ACTION_DIAL`, never `ACTION_CALL`), so Mono needs no new permission
 * and the user still presses call.
 */
@Singleton
class ContactReacher @Inject constructor(@ApplicationContext private val context: Context) {
    fun reach(contact: Contact, how: ContactReach): ActionResult {
        val intent = when (how) {
            ContactReach.Call -> contact.phone?.let { Intent(Intent.ACTION_DIAL, uri("tel", it)) }
            ContactReach.Message -> contact.phone?.let { Intent(Intent.ACTION_SENDTO, uri("smsto", it)) }
            ContactReach.Email -> contact.email?.let { Intent(Intent.ACTION_SENDTO, uri("mailto", it)) }
        } ?: return ActionResult.Failed(R.string.search_error_no_reach)
        return try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            ActionResult.Done
        } catch (_: ActivityNotFoundException) {
            ActionResult.Failed(how.noApp)
        }
    }

    private fun uri(scheme: String, target: String): Uri = Uri.fromParts(scheme, target, null)
}
