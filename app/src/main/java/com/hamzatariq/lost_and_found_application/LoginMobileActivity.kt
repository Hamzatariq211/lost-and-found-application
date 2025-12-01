package com.hamzatariq.lost_and_found_application

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.hamzatariq.lost_and_found_application.viewmodel.PhoneAuthViewModel
import com.hamzatariq.lost_and_found_application.viewmodel.PhoneAuthViewModelFactory

class LoginMobileActivity : AppCompatActivity() {

    private lateinit var phoneAuthViewModel: PhoneAuthViewModel
    private lateinit var mobileNumberInput: TextInputEditText
    private lateinit var otpInput: TextInputEditText
    private lateinit var getOtpText: TextView
    private lateinit var signInButton: MaterialButton

    private var isOtpSent = false
    private var countDownTimer: CountDownTimer? = null
    private var timeRemaining = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_mobile)

        // Hide the action bar for clean look
        supportActionBar?.hide()

        // Initialize ViewModel
        phoneAuthViewModel = ViewModelProvider(this, PhoneAuthViewModelFactory(this))
            .get(PhoneAuthViewModel::class.java)

        // Initialize views
        mobileNumberInput = findViewById(R.id.mobileNumberInput)
        otpInput = findViewById(R.id.otpInput)
        getOtpText = findViewById(R.id.getOtpText)
        signInButton = findViewById(R.id.signInButton)

        // Observe phone auth state
        phoneAuthViewModel.phoneAuthState.observe(this) { state ->
            android.util.Log.d("LoginMobileActivity", "Phone auth state changed: $state")
            when (state) {
                is PhoneAuthViewModel.PhoneAuthState.Loading -> {
                    signInButton.isEnabled = false
                    signInButton.text = if (isOtpSent) "Verifying OTP..." else "Sending OTP..."
                }
                is PhoneAuthViewModel.PhoneAuthState.OtpSent -> {
                    isOtpSent = true
                    signInButton.isEnabled = true
                    signInButton.text = "Verify OTP"
                    mobileNumberInput.isEnabled = false
                    otpInput.isEnabled = true
                    otpInput.requestFocus()
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    startResendTimer()
                }
                is PhoneAuthViewModel.PhoneAuthState.Success -> {
                    android.util.Log.d("LoginMobileActivity", "Phone auth successful, navigating to HomeActivity")
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }, 500)
                }
                is PhoneAuthViewModel.PhoneAuthState.Error -> {
                    signInButton.isEnabled = true
                    signInButton.text = if (isOtpSent) "Verify OTP" else "Send OTP"
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                PhoneAuthViewModel.PhoneAuthState.Idle -> {
                    signInButton.isEnabled = true
                    signInButton.text = "Send OTP"
                }
            }
        }

        // Handle Get OTP text click
        getOtpText.setOnClickListener {
            if (isOtpSent && timeRemaining > 0) {
                Toast.makeText(this, "Please wait before requesting a new OTP", Toast.LENGTH_SHORT).show()
            } else {
                sendOtp()
            }
        }

        // Handle Sign In button click
        signInButton.setOnClickListener {
            if (isOtpSent) {
                verifyOtp()
            } else {
                sendOtp()
            }
        }

        // Handle back button
        val backButton = findViewById<android.widget.ImageView>(R.id.backButton)
        backButton?.setOnClickListener {
            finish()
        }
    }

    private fun sendOtp() {
        val phoneNumber = mobileNumberInput.text.toString().trim()

        // Validation
        when {
            phoneNumber.isEmpty() -> {
                mobileNumberInput.error = "Mobile number is required"
                return
            }
            phoneNumber.length < 10 -> {
                mobileNumberInput.error = "Invalid mobile number"
                return
            }
        }

        // Format phone number with country code if not present
        val formattedPhoneNumber = if (phoneNumber.startsWith("+")) {
            phoneNumber
        } else if (phoneNumber.startsWith("0")) {
            "+92${phoneNumber.substring(1)}"
        } else {
            "+92$phoneNumber"
        }

        android.util.Log.d("LoginMobileActivity", "Sending OTP to: $formattedPhoneNumber")
        phoneAuthViewModel.sendOtp(formattedPhoneNumber, this)
    }

    private fun verifyOtp() {
        val phoneNumber = mobileNumberInput.text.toString().trim()
        val otp = otpInput.text.toString().trim()

        // Validation
        when {
            otp.isEmpty() -> {
                otpInput.error = "OTP is required"
                return
            }
            otp.length != 6 -> {
                otpInput.error = "OTP must be 6 digits"
                return
            }
        }

        // Format phone number
        val formattedPhoneNumber = if (phoneNumber.startsWith("+")) {
            phoneNumber
        } else if (phoneNumber.startsWith("0")) {
            "+92${phoneNumber.substring(1)}"
        } else {
            "+92$phoneNumber"
        }

        android.util.Log.d("LoginMobileActivity", "Verifying OTP: $otp")
        phoneAuthViewModel.verifyOtp(formattedPhoneNumber, otp)
    }

    private fun startResendTimer() {
        countDownTimer?.cancel()
        timeRemaining = 60000 // 60 seconds

        countDownTimer = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                val seconds = millisUntilFinished / 1000
                getOtpText.text = "Resend OTP in ${seconds}s"
                getOtpText.isEnabled = false
            }

            override fun onFinish() {
                getOtpText.text = "Resend OTP"
                getOtpText.isEnabled = true
                timeRemaining = 0
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}


