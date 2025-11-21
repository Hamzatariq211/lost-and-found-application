package com.hamzatariq.lost_and_found_application

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Hide the action bar for clean look
        supportActionBar?.hide()

        // Data for items
        val items = listOf(
            Triple("Water Bottle", "Found in: C-110", "A blue water bottle found in the cafeteria"),
            Triple("Watch", "Found in: Cafe", "A silver watch left at the cafe"),
            Triple("Notebook", "Found in: D-312", "A blue notebook found in the classroom"),
            Triple("Charger", "Found in: Rawal-1", "A white phone charger found in the lounge")
        )

        // Set click listeners for item cards
        val waterBottleItem = findViewById<LinearLayout>(R.id.waterBottleItem)
        val watchItem = findViewById<LinearLayout>(R.id.watchItem)
        val notebookItem = findViewById<LinearLayout>(R.id.notebookItem)
        val chargerItem = findViewById<LinearLayout>(R.id.chargerItem)

        waterBottleItem?.setOnClickListener {
            showProductDialog(items[0].first, items[0].second, items[0].third)
        }

        watchItem?.setOnClickListener {
            showProductDialog(items[1].first, items[1].second, items[1].third)
        }

        notebookItem?.setOnClickListener {
            showProductDialog(items[2].first, items[2].second, items[2].third)
        }

        chargerItem?.setOnClickListener {
            showProductDialog(items[3].first, items[3].second, items[3].third)
        }

        // Handle profile icon click to navigate to settings
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Handle add button click to navigate to Add Post
        val addButton = findViewById<FrameLayout>(R.id.addButton)
        addButton.setOnClickListener {
            startActivity(Intent(this, AddPostActivity::class.java))
        }
    }

    private fun showProductDialog(productName: String, location: String, description: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_product_details, null)

        val productNameView = dialogView.findViewById<android.widget.TextView>(R.id.dialogProductName)
        val productLocationView = dialogView.findViewById<android.widget.TextView>(R.id.dialogProductLocation)
        val productDescriptionView = dialogView.findViewById<android.widget.TextView>(R.id.dialogProductDescription)
        val closeButton = dialogView.findViewById<MaterialButton>(R.id.dialogCloseButton)
        val contactButton = dialogView.findViewById<MaterialButton>(R.id.dialogContactButton)

        productNameView.text = productName
        productLocationView.text = location
        productDescriptionView.text = description

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        contactButton.setOnClickListener {
            // TODO: Implement contact functionality
            dialog.dismiss()
        }

        dialog.show()
    }
}
