package com.hamzatariq.lost_and_found_application

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Hide the action bar for clean look
        supportActionBar?.hide()

        // Handle click on "Login" link to go back to login screen
        val loginLink = findViewById<android.widget.TextView>(R.id.loginLink)
        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Handle click on "Sign Up" button
        val signUpButton = findViewById<MaterialButton>(R.id.signUpButton)
        signUpButton.setOnClickListener {
            startActivity(Intent(this, LoadingActivity::class.java))
        }
    }
}
