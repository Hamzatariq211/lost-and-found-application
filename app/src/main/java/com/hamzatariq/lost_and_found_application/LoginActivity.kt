package com.hamzatariq.lost_and_found_application

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.hamzatariq.lost_and_found_application.viewmodel.AuthViewModel
import com.hamzatariq.lost_and_found_application.viewmodel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var signInButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Hide the action bar for clean look
        supportActionBar?.hide()

        // Initialize ViewModel
        authViewModel = ViewModelProvider(this, AuthViewModelFactory(this)).get(AuthViewModel::class.java)

        // Initialize views
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        signInButton = findViewById(R.id.signInButton)

        // Observe auth state - MUST be set up before any login call
        authViewModel.authState.observe(this) { state ->
            android.util.Log.d("LoginActivity", "Auth state changed: $state")
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    signInButton.isEnabled = false
                    signInButton.text = "Signing In..."
                }
                is AuthViewModel.AuthState.Success -> {
                    android.util.Log.d("LoginActivity", "Login successful, navigating to HomeActivity")
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    // Add a small delay to ensure UI updates
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }, 500)
                }
                is AuthViewModel.AuthState.Error -> {
                    signInButton.isEnabled = true
                    signInButton.text = "Sign In"
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                AuthViewModel.AuthState.Idle -> {
                    signInButton.isEnabled = true
                    signInButton.text = "Sign In"
                }
            }
        }

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
        signInButton.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // Validation
        when {
            username.isEmpty() -> {
                usernameInput.error = "Username or email is required"
                return
            }
            password.isEmpty() -> {
                passwordInput.error = "Password is required"
                return
            }
        }

        // Perform login
        authViewModel.login(username, password)
    }
}
