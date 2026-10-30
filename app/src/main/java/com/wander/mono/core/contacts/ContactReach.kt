package com.wander.mono.core.contacts

import androidx.annotation.StringRes
import com.wander.mono.R

/** A way to reach a contact from search ([ContactReacher]), with the error when no app takes it. */
enum class ContactReach(@StringRes val noApp: Int) {
    Call(R.string.search_error_no_dialer),
    Message(R.string.search_error_no_messages),
    Email(R.string.search_error_no_email),
}

/** The ways [contact] can be reached: Call and Message with a phone number, Email with an address. */
fun reachOf(contact: Contact): List<ContactReach> = buildList {
    if (contact.phone != null) addAll(listOf(ContactReach.Call, ContactReach.Message))
    if (contact.email != null) add(ContactReach.Email)
}
