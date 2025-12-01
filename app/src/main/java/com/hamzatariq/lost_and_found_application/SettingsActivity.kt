package com.hamzatariq.lost_and_found_application

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.hamzatariq.lost_and_found_application.api.ApiConfig
import com.hamzatariq.lost_and_found_application.utils.SharedPreferencesManager
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var fullNameInput: TextInputEditText
    private lateinit var usernameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var progressBar: ProgressBar

    private var currentUserId: Int = 0

    companion object {
        private const val TAG = "SettingsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Hide the action bar for clean look
        supportActionBar?.hide()

        prefsManager = SharedPreferencesManager(this)
        currentUserId = prefsManager.getUserId()

        // Initialize views
        initializeViews()

        // Load user profile
        loadUserProfile()

        // Handle back button click
        val backButton = findViewById<android.widget.ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Handle save button click
        val saveButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.saveButton)
        saveButton.setOnClickListener {
            updateUserProfile()
        }

        // Handle logout button click
        val logoutButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.logoutButton)
        logoutButton?.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun initializeViews() {
        fullNameInput = findViewById(R.id.fullNameInput)
        usernameInput = findViewById(R.id.usernameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        passwordInput = findViewById(R.id.passwordInput)

        // Create and add progress bar
        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
            isIndeterminate = true
        }
    }

    private fun loadUserProfile() {
        showLoading(true)

        Thread {
            try {
                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.GET_PROFILE) + "?user_id=$currentUserId")
                Log.d(TAG, "Loading profile from: $url")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().readText()

                Log.d(TAG, "Profile response code: $responseCode")
                Log.d(TAG, "Profile response: $response")

                val jsonResponse = JSONObject(response)

                runOnUiThread {
                    showLoading(false)

                    if (jsonResponse.getBoolean("success")) {
                        val userData = jsonResponse.getJSONObject("data")

                        // Populate fields
                        fullNameInput.setText(userData.getString("full_name"))
                        usernameInput.setText(userData.getString("username"))
                        emailInput.setText(userData.getString("email"))
                        phoneInput.setText(userData.optString("mobile_number", ""))

                        // Update SharedPreferences
                        prefsManager.saveUserData(
                            userData.getInt("user_id"),
                            userData.getString("username"),
                            userData.getString("email"),
                            userData.getString("full_name"),
                            userData.optString("mobile_number", "")
                        )

                        Log.d(TAG, "Profile loaded successfully")
                    } else {
                        val message = jsonResponse.getString("message")
                        Toast.makeText(this, "Failed to load profile: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile: ${e.message}", e)
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun updateUserProfile() {
        // Validate inputs
        val fullName = fullNameInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        when {
            fullName.isEmpty() -> {
                fullNameInput.error = "Full name is required"
                fullNameInput.requestFocus()
                return
            }
            username.isEmpty() -> {
                usernameInput.error = "Username is required"
                usernameInput.requestFocus()
                return
            }
            email.isEmpty() -> {
                emailInput.error = "Email is required"
                emailInput.requestFocus()
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailInput.error = "Invalid email format"
                emailInput.requestFocus()
                return
            }
        }

        showLoading(true)

        Thread {
            try {
                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.UPDATE_PROFILE))
                Log.d(TAG, "Updating profile at: $url")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.setRequestProperty("Content-Type", "application/json")

                // Build JSON payload
                val jsonPayload = JSONObject().apply {
                    put("user_id", currentUserId)
                    put("full_name", fullName)
                    put("username", username)
                    put("email", email)
                    put("mobile_number", phone)
                    if (password.isNotEmpty()) {
                        put("password", password)
                    }
                }

                connection.outputStream.write(jsonPayload.toString().toByteArray())

                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().readText()

                Log.d(TAG, "Update response code: $responseCode")
                Log.d(TAG, "Update response: $response")

                val jsonResponse = JSONObject(response)

                runOnUiThread {
                    showLoading(false)

                    if (jsonResponse.getBoolean("success")) {
                        // Update SharedPreferences
                        prefsManager.saveUserData(currentUserId, username, email, fullName, phone)

                        // Clear password field
                        passwordInput.setText("")

                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                        // Show success dialog
                        AlertDialog.Builder(this)
                            .setTitle("Success")
                            .setMessage("Your profile has been updated successfully!")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    } else {
                        val message = jsonResponse.getString("message")
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile: ${e.message}", e)
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun showLoading(show: Boolean) {
        val saveButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.saveButton)

        if (show) {
            saveButton.isEnabled = false
            saveButton.text = "Updating..."
        } else {
            saveButton.isEnabled = true
            saveButton.text = getString(R.string.save_changes)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()

        // Clear all local data
        prefsManager.clearAll()

        // Show confirmation
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to Login and clear back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
