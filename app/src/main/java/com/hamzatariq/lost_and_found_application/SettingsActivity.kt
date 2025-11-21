package com.hamzatariq.lost_and_found_application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Hide the action bar for clean look
        supportActionBar?.hide()

        // Handle back button click
        val backButton = findViewById<android.widget.ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Handle save button click
        val saveButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.saveButton)
        saveButton.setOnClickListener {
            // TODO: Implement save functionality to backend
            finish()
        }
    }
}

