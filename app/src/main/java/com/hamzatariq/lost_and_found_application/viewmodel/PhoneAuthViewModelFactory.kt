package com.hamzatariq.lost_and_found_application.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.hamzatariq.lost_and_found_application.api.RetrofitClient
import com.hamzatariq.lost_and_found_application.repository.PhoneAuthRepository
import com.hamzatariq.lost_and_found_application.utils.SharedPreferencesManager

class PhoneAuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhoneAuthViewModel::class.java)) {
            val phoneAuthRepository = PhoneAuthRepository(
                FirebaseAuth.getInstance(),
                RetrofitClient.apiService,
                SharedPreferencesManager(context)
            )
            @Suppress("UNCHECKED_CAST")
            return PhoneAuthViewModel(phoneAuthRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
