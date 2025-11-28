package com.salus.widget.data

/**
 * Data class representing a contact with name and phone number.
 */
data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String
) {
    /**
     * Returns the initial letter of the contact's name for avatar display.
     */
    fun getInitial(): String {
        return name.firstOrNull()?.uppercase() ?: "?"
    }
}
