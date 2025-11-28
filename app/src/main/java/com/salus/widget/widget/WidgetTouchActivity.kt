package com.salus.widget.widget

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.salus.widget.R
import com.salus.widget.data.Contact
import com.salus.widget.data.ContactRepository

/**
 * Transparent activity that handles long-press detection for the widget.
 * When the user long-presses for 3 seconds, a random contact is called.
 */
class WidgetTouchActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var isLongPressActive = false
    private var longPressHandler: Handler? = null
    private var longPressRunnable: Runnable? = null
    private lateinit var repository: ContactRepository
    private var pendingCallContact: Contact? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_touch)

        repository = ContactRepository(this)

        // Get widget ID from intent
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setupTouchListener()
    }

    private fun setupTouchListener() {
        val container = findViewById<FrameLayout>(R.id.touch_container)
        
        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startLongPressTimer()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    cancelLongPressTimer()
                    if (!isLongPressActive) {
                        // Short tap - just close the activity
                        finish()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun startLongPressTimer() {
        isLongPressActive = false
        longPressHandler = Handler(Looper.getMainLooper())
        longPressRunnable = Runnable {
            isLongPressActive = true
            triggerRandomCall()
        }
        longPressHandler?.postDelayed(longPressRunnable!!, LONG_PRESS_DURATION_MS)
    }

    private fun cancelLongPressTimer() {
        longPressRunnable?.let { longPressHandler?.removeCallbacks(it) }
        longPressHandler = null
        longPressRunnable = null
    }

    private fun triggerRandomCall() {
        val contact = repository.getRandomContact(appWidgetId)
        
        if (contact == null) {
            Toast.makeText(this, R.string.no_contacts, Toast.LENGTH_LONG).show()
            // Use Handler to delay finish() so Toast is visible
            Handler(Looper.getMainLooper()).postDelayed({ finish() }, 1000)
            return
        }

        // Check for call permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) 
            != PackageManager.PERMISSION_GRANTED) {
            // Store the contact for use after permission is granted
            pendingCallContact = contact
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_CALL_PERMISSION
            )
            return
        }

        makePhoneCall(contact.phoneNumber)
    }

    private fun makePhoneCall(phoneNumber: String) {
        try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(callIntent)
        } catch (e: SecurityException) {
            // Fallback to dialer if call permission is denied
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(dialIntent)
        } finally {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_CALL_PERMISSION) {
            val contact = pendingCallContact
            pendingCallContact = null
            
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, make the call with the stored contact
                contact?.let { makePhoneCall(it.phoneNumber) } ?: finish()
            } else {
                // Permission denied, use dialer instead
                contact?.let {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${it.phoneNumber}")
                    }
                    startActivity(dialIntent)
                }
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelLongPressTimer()
    }

    companion object {
        private const val LONG_PRESS_DURATION_MS = 3000L
        private const val REQUEST_CALL_PERMISSION = 1001
    }
}
