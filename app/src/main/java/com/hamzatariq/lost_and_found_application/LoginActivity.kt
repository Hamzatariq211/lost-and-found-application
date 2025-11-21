package com.hamzatariq.lost_and_found_application

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Hide the action bar for clean look
        supportActionBar?.hide()

        // Handle click on "Login using Mobile Number"
        val mobileLoginText = findViewById<android.widget.TextView>(R.id.mobileLoginText)
        mobileLoginText.setOnClickListener {
            startActivity(Intent(this, LoginMobileActivity::class.java))
        }

        // Handle click on "Signup" link
        val signUpLink = findViewById<android.widget.TextView>(R.id.signUpLink)
        signUpLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Handle click on "Sign In" button
        val signInButton = findViewById<MaterialButton>(R.id.signInButton)
        signInButton.setOnClickListener {
            startActivity(Intent(this, LoadingActivity::class.java))
        }
    }
}
