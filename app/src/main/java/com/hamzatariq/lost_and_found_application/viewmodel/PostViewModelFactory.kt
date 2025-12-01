package com.hamzatariq.lost_and_found_application.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hamzatariq.lost_and_found_application.api.RetrofitClient
import com.hamzatariq.lost_and_found_application.repository.PostRepository
import com.hamzatariq.lost_and_found_application.utils.SharedPreferencesManager

class PostViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            val apiService = RetrofitClient.apiService
            val prefsManager = SharedPreferencesManager(context)
            val repository = PostRepository(apiService, prefsManager, context)
            @Suppress("UNCHECKED_CAST")
            return PostViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
