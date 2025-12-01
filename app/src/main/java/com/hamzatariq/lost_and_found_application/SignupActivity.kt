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

class SignupActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var fullNameInput: TextInputEditText
    private lateinit var usernameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var mobileInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var signUpButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Hide the action bar for clean look
        supportActionBar?.hide()

        // Initialize ViewModel
        authViewModel = ViewModelProvider(this, AuthViewModelFactory(this)).get(AuthViewModel::class.java)

        // Initialize views
        fullNameInput = findViewById(R.id.fullNameInput)
        usernameInput = findViewById(R.id.usernameInput)
        emailInput = findViewById(R.id.emailInput)
        mobileInput = findViewById(R.id.mobileInput)
        passwordInput = findViewById(R.id.passwordInput)
        signUpButton = findViewById(R.id.signUpButton)

        // Observe auth state - MUST be set up before any signup call
        authViewModel.authState.observe(this) { state ->
            android.util.Log.d("SignupActivity", "Auth state changed: $state")
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    signUpButton.isEnabled = false
                    signUpButton.text = "Creating Account..."
                }
                is AuthViewModel.AuthState.Success -> {
                    android.util.Log.d("SignupActivity", "Signup successful, navigating to HomeActivity")
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    // Add a small delay to ensure UI updates
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }, 500)
                }
                is AuthViewModel.AuthState.Error -> {
                    signUpButton.isEnabled = true
                    signUpButton.text = "Sign Up"
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                AuthViewModel.AuthState.Idle -> {
                    signUpButton.isEnabled = true
                    signUpButton.text = "Sign Up"
                }
            }
        }

        // Handle click on "Login" link to go back to login screen
        val loginLink = findViewById<android.widget.TextView>(R.id.loginLink)
        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Handle click on "Sign Up" button
        signUpButton.setOnClickListener {
            performSignup()
        }
    }

    private fun performSignup() {
        val fullName = fullNameInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val mobile = mobileInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // Validation
        when {
            fullName.isEmpty() -> {
                fullNameInput.error = "Full name is required"
                return
            }
            username.isEmpty() -> {
                usernameInput.error = "Username is required"
                return
            }
            email.isEmpty() -> {
                emailInput.error = "Email is required"
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailInput.error = "Invalid email format"
                return
            }
            mobile.isEmpty() -> {
                mobileInput.error = "Mobile number is required"
                return
            }
            password.isEmpty() -> {
                passwordInput.error = "Password is required"
                return
            }
            password.length < 6 -> {
                passwordInput.error = "Password must be at least 6 characters"
                return
            }
        }

        // Perform signup
        authViewModel.signup(fullName, username, email, mobile, password)
    }
}
