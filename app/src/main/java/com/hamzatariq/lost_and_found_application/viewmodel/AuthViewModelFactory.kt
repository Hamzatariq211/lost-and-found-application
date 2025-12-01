package com.hamzatariq.lost_and_found_application.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.hamzatariq.lost_and_found_application.api.RetrofitClient
import com.hamzatariq.lost_and_found_application.repository.AuthRepository
import com.hamzatariq.lost_and_found_application.utils.SharedPreferencesManager

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val authRepository = AuthRepository(
                RetrofitClient.apiService,
                FirebaseAuth.getInstance(),
                SharedPreferencesManager(context),
                context
            )
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
