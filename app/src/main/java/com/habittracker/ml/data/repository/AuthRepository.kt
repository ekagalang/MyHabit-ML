package com.habittracker.ml.data.repository

import com.habittracker.ml.data.local.preferences.AuthPreferences
import com.habittracker.ml.data.remote.ApiService
import com.habittracker.ml.data.remote.dto.ChangePasswordRequest
import com.habittracker.ml.data.remote.dto.LoginRequest
import com.habittracker.ml.data.remote.dto.RegisterRequest
import com.habittracker.ml.data.remote.dto.UserDto

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

class AuthRepository(
    private val apiService: ApiService,
    private val authPreferences: AuthPreferences
) {

    suspend fun login(email: String, password: String): AuthResult<UserDto> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val loginResponse = response.body()!!.data!!
                authPreferences.saveAuth(
                    token = loginResponse.token,
                    userId = loginResponse.user.id,
                    name = loginResponse.user.name,
                    email = loginResponse.user.email
                )
                AuthResult.Success(loginResponse.user)
            } else {
                val errorMsg = response.body()?.error
                    ?: response.body()?.message
                    ?: "Login failed"
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun register(name: String, email: String, password: String): AuthResult<UserDto> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, name))
            if (response.isSuccessful && response.body()?.success == true) {
                val loginResponse = response.body()!!.data!!
                authPreferences.saveAuth(
                    token = loginResponse.token,
                    userId = loginResponse.user.id,
                    name = loginResponse.user.name,
                    email = loginResponse.user.email
                )
                AuthResult.Success(loginResponse.user)
            } else {
                val errorMsg = response.body()?.error
                    ?: response.body()?.message
                    ?: "Registration failed"
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getProfile(): AuthResult<UserDto> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body()?.success == true) {
                AuthResult.Success(response.body()!!.data!!)
            } else {
                AuthResult.Error("Failed to get profile")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    fun isLoggedIn(): Boolean = authPreferences.isLoggedIn()

    fun getUserName(): String = authPreferences.getUserName()

    fun getUserEmail(): String = authPreferences.getUserEmail()

    suspend fun changePassword(oldPassword: String, newPassword: String): AuthResult<String> {
        return try {
            val response = apiService.changePassword(ChangePasswordRequest(oldPassword, newPassword))
            if (response.isSuccessful && response.body()?.success == true) {
                AuthResult.Success(response.body()?.message ?: "Password changed successfully")
            } else {
                val errorMsg = response.body()?.error
                    ?: response.body()?.message
                    ?: "Failed to change password"
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    fun logout() {
        authPreferences.clearAuth()
    }
}
