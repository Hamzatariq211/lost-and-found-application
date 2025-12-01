package com.hamzatariq.lost_and_found_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamzatariq.lost_and_found_application.api.UserData
import com.hamzatariq.lost_and_found_application.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> = _userData
    
    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val message: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }
    
    fun signup(
        fullName: String,
        username: String,
        email: String,
        mobileNumber: String,
        password: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.signup(fullName, username, email, mobileNumber, password)
            
            when (result) {
                is AuthRepository.AuthResult.Success -> {
                    _userData.value = result.userData
                    _authState.value = AuthState.Success("Signup successful!")
                }
                is AuthRepository.AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                AuthRepository.AuthResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.login(username, password)
            
            when (result) {
                is AuthRepository.AuthResult.Success -> {
                    _userData.value = result.userData
                    _authState.value = AuthState.Success("Login successful!")
                }
                is AuthRepository.AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                AuthRepository.AuthResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }
    
    fun logout() {
        authRepository.logout()
        _authState.value = AuthState.Idle
        _userData.value = null
    }
    
    fun isUserLoggedIn(): Boolean = authRepository.isUserLoggedIn()
}
