package com.hamzatariq.lost_and_found_application.viewmodel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamzatariq.lost_and_found_application.api.UserData
import com.hamzatariq.lost_and_found_application.repository.PhoneAuthRepository
import kotlinx.coroutines.launch

class PhoneAuthViewModel(private val phoneAuthRepository: PhoneAuthRepository) : ViewModel() {

    private val _phoneAuthState = MutableLiveData<PhoneAuthState>()
    val phoneAuthState: LiveData<PhoneAuthState> = _phoneAuthState

    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> = _userData

    private val _verificationId = MutableLiveData<String>()
    val verificationId: LiveData<String> = _verificationId

    sealed class PhoneAuthState {
        object Idle : PhoneAuthState()
        object Loading : PhoneAuthState()
        data class OtpSent(val message: String) : PhoneAuthState()
        data class Success(val message: String) : PhoneAuthState()
        data class Error(val message: String) : PhoneAuthState()
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
        viewModelScope.launch {
            _phoneAuthState.value = PhoneAuthState.Loading

            val result = phoneAuthRepository.sendOtp(phoneNumber, activity)

            when (result) {
                is PhoneAuthRepository.PhoneAuthResult.OtpSent -> {
                    _verificationId.value = result.verificationId
                    _phoneAuthState.value = PhoneAuthState.OtpSent("OTP sent successfully to $phoneNumber")
                }
                is PhoneAuthRepository.PhoneAuthResult.Error -> {
                    _phoneAuthState.value = PhoneAuthState.Error(result.message)
                }
                is PhoneAuthRepository.PhoneAuthResult.Success -> {
                    _userData.value = result.userData
                    _phoneAuthState.value = PhoneAuthState.Success("Auto-verified!")
                }
                PhoneAuthRepository.PhoneAuthResult.Loading -> {
                    _phoneAuthState.value = PhoneAuthState.Loading
                }
            }
        }
    }

    fun verifyOtp(phoneNumber: String, otp: String) {
        viewModelScope.launch {
            _phoneAuthState.value = PhoneAuthState.Loading

            val result = phoneAuthRepository.verifyOtpAndLogin(phoneNumber, otp)

            when (result) {
                is PhoneAuthRepository.PhoneAuthResult.Success -> {
                    _userData.value = result.userData
                    _phoneAuthState.value = PhoneAuthState.Success("Login successful!")
                }
                is PhoneAuthRepository.PhoneAuthResult.Error -> {
                    _phoneAuthState.value = PhoneAuthState.Error(result.message)
                }
                is PhoneAuthRepository.PhoneAuthResult.OtpSent -> {
                    _phoneAuthState.value = PhoneAuthState.OtpSent("OTP sent")
                }
                PhoneAuthRepository.PhoneAuthResult.Loading -> {
                    _phoneAuthState.value = PhoneAuthState.Loading
                }
            }
        }
    }

    fun resendOtp(phoneNumber: String, activity: Activity) {
        viewModelScope.launch {
            _phoneAuthState.value = PhoneAuthState.Loading

            val result = phoneAuthRepository.resendOtp(phoneNumber, activity)

            when (result) {
                is PhoneAuthRepository.PhoneAuthResult.OtpSent -> {
                    _verificationId.value = result.verificationId
                    _phoneAuthState.value = PhoneAuthState.OtpSent("OTP resent successfully")
                }
                is PhoneAuthRepository.PhoneAuthResult.Error -> {
                    _phoneAuthState.value = PhoneAuthState.Error(result.message)
                }
                else -> {
                    _phoneAuthState.value = PhoneAuthState.Error("Unexpected error")
                }
            }
        }
    }

    fun logout() {
        phoneAuthRepository.logout()
        _phoneAuthState.value = PhoneAuthState.Idle
        _userData.value = null
    }
}
