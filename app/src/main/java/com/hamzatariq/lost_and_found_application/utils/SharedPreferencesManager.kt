package com.hamzatariq.lost_and_found_application.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "lost_and_found_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_MOBILE = "mobile_number"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }
    
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }
    
    fun saveUserData(userId: Int, username: String, email: String, fullName: String, mobile: String?) {
        sharedPreferences.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_MOBILE, mobile ?: "")
            putBoolean(KEY_IS_LOGGED_IN, true)  // Set persistent login flag
            apply()
        }
    }
    
    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }
    
    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }
    
    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }
    
    fun getFullName(): String? {
        return sharedPreferences.getString(KEY_FULL_NAME, null)
    }
    
    fun getMobileNumber(): String? {
        return sharedPreferences.getString(KEY_MOBILE, null)
    }
    
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) && getUserId() != -1
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun saveFCMToken(token: String) {
        sharedPreferences.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getFCMToken(): String? {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null)
    }
}
