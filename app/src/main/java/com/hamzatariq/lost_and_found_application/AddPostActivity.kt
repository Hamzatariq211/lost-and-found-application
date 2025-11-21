package com.hamzatariq.lost_and_found_application

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class AddPostActivity : AppCompatActivity() {

    private var isLostItem = false
    private lateinit var lostItemButton: LinearLayout
    private lateinit var foundItemButton: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        // Hide the action bar for clean look
        supportActionBar?.hide()

        // Initialize views
        lostItemButton = findViewById(R.id.lostItemButton)
        foundItemButton = findViewById(R.id.foundItemButton)

        // Handle back button click
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Handle Lost Item Button click
        lostItemButton.setOnClickListener {
            isLostItem = true
            updateItemTypeSelection()
        }

        // Handle Found Item Button click
        foundItemButton.setOnClickListener {
            isLostItem = false
            updateItemTypeSelection()
        }

        // Handle image upload buttons click
        val uploadButtonOne = findViewById<LinearLayout>(R.id.uploadButtonOne)
        val uploadButtonTwo = findViewById<LinearLayout>(R.id.uploadButtonTwo)

        uploadButtonOne.setOnClickListener {
            // TODO: Implement image picker functionality for "What did you find?"
        }

        uploadButtonTwo.setOnClickListener {
            // TODO: Implement image picker functionality for "Where did you find it?"
        }

        // Handle image upload container click
        val imageUploadContainer = findViewById<android.widget.FrameLayout>(R.id.imageUploadContainer)
        imageUploadContainer.setOnClickListener {
            // TODO: Implement image picker functionality
        }

        // Handle Notify Me button click
        val notifyMeButton = findViewById<LinearLayout>(R.id.notifyMeButton)
        notifyMeButton.setOnClickListener {
            // TODO: Implement notification setup
        }

        // Handle post button click
        val postButton = findViewById<MaterialButton>(R.id.postButton)
        postButton.setOnClickListener {
            // TODO: Implement post submission to backend
            finish()
        }

        // Set default selection to Lost Item
        isLostItem = true
        updateItemTypeSelection()
    }

    private fun updateItemTypeSelection() {
        if (isLostItem) {
            // Lost item is selected - white background
            lostItemButton.setBackgroundResource(R.drawable.item_type_button_selected_bg)
            foundItemButton.setBackgroundResource(R.drawable.item_type_button_bg)
        } else {
            // Found item is selected - white background
            foundItemButton.setBackgroundResource(R.drawable.item_type_button_selected_bg)
            lostItemButton.setBackgroundResource(R.drawable.item_type_button_bg)
        }
    }
}
