package com.hamzatariq.lost_and_found_application.repository

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hamzatariq.lost_and_found_application.api.ApiService
import com.hamzatariq.lost_and_found_application.api.LoginRequest
import com.hamzatariq.lost_and_found_application.api.UserData
import com.hamzatariq.lost_and_found_application.utils.SharedPreferencesManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class PhoneAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val apiService: ApiService,
    private val prefsManager: SharedPreferencesManager
) {

    sealed class PhoneAuthResult {
        data class Success(val userData: UserData) : PhoneAuthResult()
        data class Error(val message: String) : PhoneAuthResult()
        data class OtpSent(val verificationId: String) : PhoneAuthResult()
        object Loading : PhoneAuthResult()
    }

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    /**
     * Send OTP to phone number
     */
    suspend fun sendOtp(
        phoneNumber: String,
        activity: android.app.Activity
    ): PhoneAuthResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        Log.d("PhoneAuth", "Verification completed automatically")
                        // Auto-retrieval of OTP
                        verificationId = credential.smsCode
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Log.e("PhoneAuth", "Verification failed: ${e.message}")
                        continuation.resume(PhoneAuthResult.Error(e.message ?: "Verification failed"))
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        Log.d("PhoneAuth", "OTP sent successfully")
                        this@PhoneAuthRepository.verificationId = verificationId
                        this@PhoneAuthRepository.resendToken = token
                        continuation.resume(PhoneAuthResult.OtpSent(verificationId))
                    }
                }

                val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            } catch (e: Exception) {
                Log.e("PhoneAuth", "Error sending OTP: ${e.message}")
                continuation.resume(PhoneAuthResult.Error(e.message ?: "Error sending OTP"))
            }
        }
    }

    /**
     * Verify OTP and login
     */
    suspend fun verifyOtpAndLogin(
        phoneNumber: String,
        otp: String
    ): PhoneAuthResult {
        return try {
            if (verificationId == null) {
                return PhoneAuthResult.Error("Verification ID not found. Please request OTP again.")
            }

            // Create credential with OTP
            val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)

            // Sign in with credential
            firebaseAuth.signInWithCredential(credential).await()

            // Get the phone number from Firebase user
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Try to login with MySQL API using phone number
                val loginRequest = LoginRequest(
                    username = phoneNumber,
                    password = otp
                )

                val response = apiService.login(loginRequest)

                if (response.isSuccessful && response.body()?.success == true) {
                    val userData = response.body()?.data
                    if (userData != null) {
                        saveAuthToken(userData.token)
                        saveUserData(userData)
                        PhoneAuthResult.Success(userData)
                    } else {
                        PhoneAuthResult.Error("Invalid response data")
                    }
                } else {
                    // If MySQL login fails, still allow Firebase login
                    Log.w("PhoneAuth", "MySQL login failed, but Firebase auth succeeded")
                    PhoneAuthResult.Success(
                        UserData(
                            user_id = 0,
                            username = phoneNumber,
                            email = firebaseUser.email ?: "",
                            full_name = firebaseUser.displayName ?: phoneNumber,
                            mobile_number = phoneNumber,
                            token = ""
                        )
                    )
                }
            } else {
                PhoneAuthResult.Error("Firebase authentication failed")
            }
        } catch (e: Exception) {
            Log.e("PhoneAuth", "Error verifying OTP: ${e.message}")
            PhoneAuthResult.Error(e.message ?: "Error verifying OTP")
        }
    }

    /**
     * Resend OTP
     */
    suspend fun resendOtp(
        phoneNumber: String,
        activity: android.app.Activity
    ): PhoneAuthResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                if (resendToken == null) {
                    continuation.resume(PhoneAuthResult.Error("Resend token not available"))
                    return@suspendCancellableCoroutine
                }

                val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        Log.d("PhoneAuth", "Verification completed automatically")
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Log.e("PhoneAuth", "Resend verification failed: ${e.message}")
                        continuation.resume(PhoneAuthResult.Error(e.message ?: "Resend failed"))
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        Log.d("PhoneAuth", "OTP resent successfully")
                        this@PhoneAuthRepository.verificationId = verificationId
                        this@PhoneAuthRepository.resendToken = token
                        continuation.resume(PhoneAuthResult.OtpSent(verificationId))
                    }
                }

                val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                    .setForceResendingToken(resendToken!!)
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            } catch (e: Exception) {
                Log.e("PhoneAuth", "Error resending OTP: ${e.message}")
                continuation.resume(PhoneAuthResult.Error(e.message ?: "Error resending OTP"))
            }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        clearAuthToken()
        verificationId = null
        resendToken = null
    }

    private fun saveAuthToken(token: String) {
        prefsManager.saveAuthToken(token)
        Log.d("PhoneAuth", "Token saved")
    }

    private fun saveUserData(userData: UserData) {
        prefsManager.saveUserData(
            userData.user_id,
            userData.username,
            userData.email,
            userData.full_name,
            userData.mobile_number
        )
        Log.d("PhoneAuth", "User data saved")
    }

    private fun clearAuthToken() {
        prefsManager.clearAll()
        Log.d("PhoneAuth", "Token and user data cleared")
    }
}
