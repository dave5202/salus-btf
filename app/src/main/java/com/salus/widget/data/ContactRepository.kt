package com.salus.widget.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Repository for managing widget contact selections using SharedPreferences.
 * Each widget instance can have its own set of selected contacts.
 */
class ContactRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Saves the selected contacts for a specific widget ID.
     * 
     * @param widgetId The app widget ID
     * @param contacts List of selected contacts
     */
    fun saveSelectedContacts(widgetId: Int, contacts: List<Contact>) {
        val json = gson.toJson(contacts)
        sharedPreferences.edit()
            .putString(getKey(widgetId), json)
            .apply()
    }

    /**
     * Retrieves the selected contacts for a specific widget ID.
     * 
     * @param widgetId The app widget ID
     * @return List of selected contacts, or empty list if none selected
     */
    fun getSelectedContacts(widgetId: Int): List<Contact> {
        val json = sharedPreferences.getString(getKey(widgetId), null) ?: return emptyList()
        val type = object : TypeToken<List<Contact>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Removes the saved contacts for a specific widget ID.
     * Should be called when a widget is deleted.
     * 
     * @param widgetId The app widget ID
     */
    fun deleteSelectedContacts(widgetId: Int) {
        sharedPreferences.edit()
            .remove(getKey(widgetId))
            .apply()
    }

    /**
     * Gets a random contact from the selected contacts for a widget.
     * 
     * @param widgetId The app widget ID
     * @return A randomly selected contact, or null if no contacts are configured
     */
    fun getRandomContact(widgetId: Int): Contact? {
        val contacts = getSelectedContacts(widgetId)
        return if (contacts.isNotEmpty()) {
            contacts.random()
        } else {
            null
        }
    }

    private fun getKey(widgetId: Int): String = "$KEY_PREFIX$widgetId"

    companion object {
        private const val PREFS_NAME = "salus_widget_prefs"
        private const val KEY_PREFIX = "widget_contacts_"
    }
}
