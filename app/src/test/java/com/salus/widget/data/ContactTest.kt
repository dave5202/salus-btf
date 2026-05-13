package com.salus.widget.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the Contact data class.
 */
class ContactTest {

    @Test
    fun `getInitial returns first letter uppercase`() {
        val contact = Contact(id = "1", name = "John Doe", phoneNumber = "1234567890")
        assertEquals("J", contact.getInitial())
    }

    @Test
    fun `getInitial returns question mark for empty name`() {
        val contact = Contact(id = "1", name = "", phoneNumber = "1234567890")
        assertEquals("?", contact.getInitial())
    }

    @Test
    fun `getInitial returns uppercase for lowercase name`() {
        val contact = Contact(id = "1", name = "alice", phoneNumber = "1234567890")
        assertEquals("A", contact.getInitial())
    }

    @Test
    fun `contact equality works correctly`() {
        val contact1 = Contact(id = "1", name = "John", phoneNumber = "123")
        val contact2 = Contact(id = "1", name = "John", phoneNumber = "123")
        val contact3 = Contact(id = "2", name = "John", phoneNumber = "123")

        assertEquals(contact1, contact2)
        assertNotEquals(contact1, contact3)
    }
}
