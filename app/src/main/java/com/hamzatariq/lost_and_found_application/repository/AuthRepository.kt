package com.hamzatariq.lost_and_found_application.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.hamzatariq.lost_and_found_application.api.ApiService
import com.hamzatariq.lost_and_found_application.api.AuthResponse
import com.hamzatariq.lost_and_found_application.api.LoginRequest
import com.hamzatariq.lost_and_found_application.api.SignupRequest
import com.hamzatariq.lost_and_found_application.api.UserData
import com.hamzatariq.lost_and_found_application.firebase.DataSyncService
import com.hamzatariq.lost_and_found_application.utils.SharedPreferencesManager
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val apiService: ApiService,
    private val firebaseAuth: FirebaseAuth,
    private val prefsManager: SharedPreferencesManager,
    private val context: Context
) {

    private val dataSyncService = DataSyncService(context)

    sealed class AuthResult {
        data class Success(val userData: UserData) : AuthResult()
        data class Error(val message: String) : AuthResult()
        object Loading : AuthResult()
    }
    
    suspend fun signup(
        fullName: String,
        username: String,
        email: String,
        mobileNumber: String,
        password: String
    ): AuthResult {
        return try {
            // First, create user in Firebase
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            
            // Then, register in MySQL via API
            val signupRequest = SignupRequest(
                full_name = fullName,
                username = username,
                email = email,
                mobile_number = mobileNumber,
                password = password
            )
            
            val response = apiService.signup(signupRequest)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val userData = response.body()?.data
                if (userData != null) {
                    // Save token and user data locally
                    saveAuthToken(userData.token)
                    saveUserData(userData)

                    // Sync user data to Firebase Firestore
                    dataSyncService.syncUserToFirebase(userData.user_id)
                    Log.d("AuthRepository", "User synced to Firebase after signup")

                    AuthResult.Success(userData)
                } else {
                    AuthResult.Error("Invalid response data")
                }
            } else {
                AuthResult.Error(response.body()?.message ?: "Signup failed")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Signup error: ${e.message}")
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    suspend fun login(
        username: String,
        password: String
    ): AuthResult {
        return try {
            // First, try to login with MySQL API
            val loginRequest = LoginRequest(
                username = username,
                password = password
            )
            
            val response = apiService.login(loginRequest)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val userData = response.body()?.data
                if (userData != null) {
                    // Try to login with Firebase using email
                    try {
                        firebaseAuth.signInWithEmailAndPassword(userData.email, password).await()
                    } catch (e: Exception) {
                        Log.w("AuthRepository", "Firebase login failed: ${e.message}")
                        // Continue anyway, MySQL auth is successful
                    }
                    
                    // Save token and user data locally
                    saveAuthToken(userData.token)
                    saveUserData(userData)

                    // Sync user data to Firebase Firestore
                    dataSyncService.syncUserToFirebase(userData.user_id)
                    Log.d("AuthRepository", "User synced to Firebase after login")

                    AuthResult.Success(userData)
                } else {
                    AuthResult.Error("Invalid response data")
                }
            } else {
                AuthResult.Error(response.body()?.message ?: "Login failed")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error: ${e.message}")
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    fun logout() {
        firebaseAuth.signOut()
        clearAuthToken()
    }
    
    fun isUserLoggedIn(): Boolean {
        return getAuthToken() != null && firebaseAuth.currentUser != null
    }
    
    fun getCurrentUser() = firebaseAuth.currentUser
    
    private fun saveAuthToken(token: String) {
        prefsManager.saveAuthToken(token)
        Log.d("AuthRepository", "Token saved")
    }
    
    private fun getAuthToken(): String? {
        return prefsManager.getAuthToken()
    }
    
    private fun saveUserData(userData: UserData) {
        prefsManager.saveUserData(
            userData.user_id,
            userData.username,
            userData.email,
            userData.full_name,
            userData.mobile_number
        )
        Log.d("AuthRepository", "User data saved")
    }
    
    private fun clearAuthToken() {
        prefsManager.clearAll()
        Log.d("AuthRepository", "Token and user data cleared")
    }
}
