package com.salus.widget.util

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import com.salus.widget.data.Contact

/**
 * Utility class for loading contacts from the device's contact provider.
 */
object ContactLoader {

    /**
     * Loads all contacts with phone numbers from the device.
     * 
     * @param contentResolver The content resolver to query contacts
     * @return List of contacts sorted alphabetically by name
     */
    fun loadContacts(contentResolver: ContentResolver): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.let {
                val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                val seenIds = mutableSetOf<String>()
                while (it.moveToNext()) {
                    val id = it.getString(idIndex) ?: continue
                    val name = it.getString(nameIndex) ?: continue
                    val number = it.getString(numberIndex) ?: continue

                    // Use composite key to avoid duplicates (same contact, same number)
                    val uniqueKey = "$id-${number.filter { c -> c.isDigit() }}"
                    if (uniqueKey !in seenIds) {
                        seenIds.add(uniqueKey)
                        contacts.add(Contact(id = uniqueKey, name = name, phoneNumber = number))
                    }
                }
            }
        } finally {
            cursor?.close()
        }

        return contacts
    }
}
