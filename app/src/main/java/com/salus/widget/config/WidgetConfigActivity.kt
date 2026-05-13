package com.salus.widget.config

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.salus.widget.R
import com.salus.widget.data.Contact
import com.salus.widget.data.ContactRepository
import com.salus.widget.util.ContactLoader
import com.salus.widget.widget.SalusWidgetProvider

/**
 * Configuration activity for the Salus widget.
 * Allows users to select contacts that will be randomly called when the widget is triggered.
 */
class WidgetConfigActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var repository: ContactRepository
    private lateinit var adapter: ContactsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var selectedCountText: TextView
    private lateinit var searchEditText: EditText

    private var allContacts: List<Contact> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED by default
        setResult(RESULT_CANCELED)

        setContentView(R.layout.activity_widget_config)

        // Get the widget ID from the intent
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        // If no valid widget ID, finish immediately
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        repository = ContactRepository(this)
        setupViews()
        checkPermissionAndLoadContacts()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.contacts_recycler_view)
        emptyView = findViewById(R.id.empty_view)
        selectedCountText = findViewById(R.id.selected_count_text)
        searchEditText = findViewById(R.id.search_edit_text)

        val saveButton = findViewById<Button>(R.id.save_button)
        val cancelButton = findViewById<Button>(R.id.cancel_button)

        // Setup RecyclerView
        adapter = ContactsAdapter { selectedContacts ->
            updateSelectedCount(selectedContacts.size)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Setup search functionality
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterContacts(s?.toString() ?: "")
            }
        })

        // Setup button listeners
        saveButton.setOnClickListener { saveAndFinish() }
        cancelButton.setOnClickListener { finish() }
    }

    private fun checkPermissionAndLoadContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_CONTACTS_PERMISSION
            )
        } else {
            loadContacts()
        }
    }

    private fun loadContacts() {
        allContacts = ContactLoader.loadContacts(contentResolver)

        if (allContacts.isEmpty()) {
            showEmptyView(getString(R.string.no_contacts_selected))
        } else {
            showContactsList()
            adapter.submitList(allContacts)

            // Load previously selected contacts for this widget
            val savedContacts = repository.getSelectedContacts(appWidgetId)
            if (savedContacts.isNotEmpty()) {
                // Find matching contacts in the loaded list
                val savedIds = savedContacts.map { it.id }.toSet()
                val matchingContacts = allContacts.filter { it.id in savedIds }.toSet()
                adapter.setSelectedContacts(matchingContacts)
            }
        }
    }

    private fun filterContacts(query: String) {
        if (query.isEmpty()) {
            adapter.submitList(allContacts)
        } else {
            val filtered = allContacts.filter { contact ->
                contact.name.contains(query, ignoreCase = true) ||
                contact.phoneNumber.contains(query)
            }
            adapter.submitList(filtered)
        }
    }

    private fun updateSelectedCount(count: Int) {
        selectedCountText.text = if (count == 0) {
            getString(R.string.no_contacts_selected)
        } else {
            getString(R.string.contacts_selected, count)
        }
    }

    private fun showEmptyView(message: String) {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        emptyView.text = message
    }

    private fun showContactsList() {
        recyclerView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
    }

    private fun saveAndFinish() {
        val selectedContacts = adapter.getSelectedContacts().toList()

        if (selectedContacts.isEmpty()) {
            Toast.makeText(this, R.string.no_contacts_selected, Toast.LENGTH_SHORT).show()
            return
        }

        // Save selected contacts
        repository.saveSelectedContacts(appWidgetId, selectedContacts)

        // Update the widget
        val appWidgetManager = AppWidgetManager.getInstance(this)
        SalusWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)

        // Set the result and finish
        val resultIntent = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CONTACTS_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts()
            } else {
                showEmptyView(getString(R.string.permission_required))
            }
        }
    }

    companion object {
        private const val REQUEST_CONTACTS_PERMISSION = 1000
    }
}
