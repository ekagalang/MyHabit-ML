package com.habittracker.ml.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.ml.data.local.preferences.AuthPreferences
import com.habittracker.ml.data.remote.ApiClient
import com.habittracker.ml.data.repository.AuthRepository
import com.habittracker.ml.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val errorMessage: String? = null,
    val navigateToHome: Boolean = false,
    val passwordChangeSuccess: Boolean = false,
    val isChangingPassword: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authPreferences = AuthPreferences(application)
    private val apiService = ApiClient.getService(application)
    private val authRepository = AuthRepository(apiService, authPreferences)

    private val _uiState = MutableStateFlow(AuthUiState(
        isLoggedIn = authRepository.isLoggedIn(),
        userName = authRepository.getUserName(),
        userEmail = authRepository.getUserEmail()
    ))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        userName = result.data.name,
                        userEmail = result.data.email,
                        navigateToHome = true
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all fields")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(errorMessage = "Passwords do not match")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = authRepository.register(name, email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        userName = result.data.name,
                        userEmail = result.data.email,
                        navigateToHome = true
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        ApiClient.resetClient()
        _uiState.value = AuthUiState()
    }

    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        if (oldPassword.isBlank() || newPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all fields")
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.value = _uiState.value.copy(errorMessage = "New passwords do not match")
            return
        }
        if (newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "New password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChangingPassword = true, errorMessage = null)

            when (val result = authRepository.changePassword(oldPassword, newPassword)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isChangingPassword = false,
                        passwordChangeSuccess = true
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isChangingPassword = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearPasswordChangeSuccess() {
        _uiState.value = _uiState.value.copy(passwordChangeSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onNavigatedToHome() {
        _uiState.value = _uiState.value.copy(navigateToHome = false)
    }
}
